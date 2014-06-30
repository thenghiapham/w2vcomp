package io.sentence;

import io.MaltInputStream;
import io.sentence.SentenceInputStream;
import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;

import common.DataStructureUtils;

import vocab.Vocab;
import dependency.DependencyNode;
import dependency.RawPhraseEntry;

public class MaltSentenceInputStream implements SentenceInputStream {
    protected MaltInputStream inputStream;
    int[]                     sentence;
    Phrase[]                  phrases;
    long                      wordCount = 0;

    public MaltSentenceInputStream(String fileName) throws IOException {
        inputStream = new MaltInputStream(fileName, DependencyNode.LEMMA_POS,
                true);
    }

    public MaltSentenceInputStream(MaltInputStream inputStream) {
        this.inputStream = inputStream;
    }

    protected void processRawWordAndPhrases(String[] words,
            RawPhraseEntry[] rawPhrases, Vocab vocab) {
        // first process single words
        // keep track where the position to put the phrase
        int[] newIndices = new int[words.length];
        // keep track of whether the component of the phrase is still in the
        // sentence
        int[] inOutIndices = new int[words.length];
        ArrayList<Integer> wordIndices = new ArrayList<Integer>();
        int newIndex = 0;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            int wordIndex = vocab.getWordIndex(word);
            if (wordIndex != -1) {
                inOutIndices[i] = newIndex;
                newIndices[i] = newIndex;
                wordIndices.add(wordIndex);
                newIndex++;
                wordCount++;
            }
            // set those words'positions that are not in vocab to -1
            else {
                inOutIndices[i] = -1;
                newIndices[i] = newIndex;
            }
        }

        // endOfSentence
        wordCount++;

        sentence = DataStructureUtils.intListToArray(wordIndices);

        ArrayList<Phrase> phraseList = new ArrayList<Phrase>();
        for (RawPhraseEntry rawPhrase : rawPhrases) {
            String phraseString = rawPhrase.phrase;
            int phraseIndex = vocab.getWordIndex(phraseString);
            if (phraseIndex != -1) {
                wordCount++;
                int startPosition = newIndices[rawPhrase.startPosition];
                int endPosition = newIndices[rawPhrase.endPosition];
                int[] oldComponentPosition = rawPhrase.componentPositions;
                int[] componentPositions = new int[oldComponentPosition.length];
                for (int i = 0; i < componentPositions.length; i++) {
                    componentPositions[i] = inOutIndices[oldComponentPosition[i]];
                }
                Phrase phrase = new Phrase(phraseIndex, startPosition,
                        endPosition, componentPositions);
                // System.out.println(rawPhrase.toString());
                // System.out.println(phrase.toString());
                phraseList.add(phrase);
            }
        }
        phrases = DataStructureUtils.phraseListToArray(phraseList);
    }

    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        boolean hasNextSentence = inputStream.readNextSentence();
        String[] words = inputStream.getSingleWords();
        RawPhraseEntry[] rawPhrases = inputStream.getRawPhrases();
        while (hasNextSentence && words.length == 0 && rawPhrases.length == 0) {
            hasNextSentence = inputStream.readNextSentence();
            words = inputStream.getSingleWords();
            rawPhrases = inputStream.getRawPhrases();
        }
        processRawWordAndPhrases(words, rawPhrases, vocab);
        return hasNextSentence;
    }

    @Override
    public int[] getCurrentSentence() throws IOException {
        return sentence;
    }

    @Override
    public Phrase[] getCurrentPhrases() throws IOException {
        return phrases;
    }

    @Override
    public long getWordCount() {
        return wordCount;
    }

}
