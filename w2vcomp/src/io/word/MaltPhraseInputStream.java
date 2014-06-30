package io.word;

import io.MaltInputStream;

import java.io.IOException;

import dependency.RawPhraseEntry;

public class MaltPhraseInputStream implements WordInputStream {
    MaltInputStream inputStream;
    int             currentPosition  = 1;
    boolean         reachedEndOfFile = false;
    String[]        words;

    public MaltPhraseInputStream(MaltInputStream inputStream) {
        this.inputStream = inputStream;
        words = new String[0];
    }

    protected boolean readNextSentence() throws IOException {
        currentPosition = 0;
        boolean result = inputStream.readNextSentence();
        String[] singleWords = inputStream.getSingleWords();
        RawPhraseEntry[] rawPhrases = inputStream.getRawPhrases();
        words = new String[singleWords.length + rawPhrases.length];
        System.arraycopy(singleWords, 0, words, 0, singleWords.length);
        int phraseOffset = singleWords.length;
        for (int i = 0; i < rawPhrases.length; i++) {
            words[i + phraseOffset] = rawPhrases[i].phrase;
        }
        return result;
    }

    @Override
    public String readWord() throws IOException {
        if (currentPosition == words.length + 1) {
            // for some reason there are empty sentences

            boolean hasNextSentence = readNextSentence();
            while (hasNextSentence && words.length == 0) {
                hasNextSentence = readNextSentence();
            }
        }

        if (currentPosition == words.length) {
            if (words.length == 0) {
                reachedEndOfFile = true;
                return "";
            } else {
                currentPosition++;
                return "</s>";
            }
        }

        String word = "";
        if (currentPosition < words.length) {
            word = words[currentPosition];
            currentPosition++;

        }
        return word;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public boolean endOfFile() {
        return reachedEndOfFile;
    }

}
