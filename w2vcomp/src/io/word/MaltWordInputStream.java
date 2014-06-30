package io.word;

import io.MaltInputStream;

import java.io.IOException;

public class MaltWordInputStream implements WordInputStream {
    MaltInputStream inputStream;
    int             currentPosition  = 1;
    boolean         reachedEndOfFile = false;
    String[]        words;

    public MaltWordInputStream(MaltInputStream inputStream) {
        this.inputStream = inputStream;
        words = new String[0];
    }

    protected boolean readNextSentence() throws IOException {
        currentPosition = 0;
        boolean result = inputStream.readNextSentence();
        words = inputStream.getSingleWords();
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
