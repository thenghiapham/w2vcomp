package io.sentence;

import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import common.DataStructureUtils;

import vocab.Vocab;
import vocab.VocabEntry;

public class SubSamplingSentenceInputStream implements SentenceInputStream {

    SentenceInputStream inputStream;
    float               frequencyThreshold;
    int[]               sentence;
    Phrase[]            phrases;
    Random              rand = new Random();

    public SubSamplingSentenceInputStream(SentenceInputStream inputStream,
            float frequencyThreshold) {
        this.inputStream = inputStream;
        this.frequencyThreshold = frequencyThreshold;
    }

    protected boolean isSampled(long count, long totalCount) {
        float randomThreshold = (float) (Math.sqrt(count
                / (frequencyThreshold * totalCount)) + 1)
                * (frequencyThreshold * totalCount) / count;
        if (randomThreshold >= rand.nextFloat() * 2) {
            return true;
        } else {
            return false;
        }
    }

    protected void filterSentence(int[] unFilteredSentence,
            Phrase[] unFilteredPhrases, Vocab vocab) {
        ArrayList<Integer> filteredIndices = new ArrayList<Integer>();
        long totalCount = vocab.getTrainWords();
        int[] newPositions = new int[unFilteredSentence.length];
        int newPosition = 0;
        for (int i = 0; i < unFilteredSentence.length; i++) {
            int vocabEntryIndex = unFilteredSentence[i];
            VocabEntry entry = vocab.getEntry(vocabEntryIndex);
            long count = entry.frequency;

            if (isSampled(count, totalCount)) {
                filteredIndices.add(vocabEntryIndex);
                newPositions[i] = newPosition;
                newPosition++;
            }
            // set those words'positions that are not in vocab to -1
            else {
                newPositions[i] = Integer.MIN_VALUE;
            }
        }
        sentence = DataStructureUtils.intListToArray(filteredIndices);

        ArrayList<Phrase> fileterPhraseList = new ArrayList<Phrase>();
        for (Phrase unFilteredPhrase : unFilteredPhrases) {
            int phraseType = unFilteredPhrase.phraseType;
            int startPosition = newPositions[unFilteredPhrase.startPosition];
            int endPosition = newPositions[unFilteredPhrase.endPosition];
            // TODO: check if this condition is correct
            if (endPosition - startPosition == unFilteredPhrase.endPosition - unFilteredPhrase.startPosition) {
                Phrase phrase = new Phrase(phraseType, startPosition,
                        endPosition, unFilteredPhrase.tree);
                fileterPhraseList.add(phrase);
            }
        }
        phrases = DataStructureUtils.phraseListToArray(fileterPhraseList);
    }

    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        boolean hasNextSentence = inputStream.readNextSentence(vocab);
        int[] unFilteredSentence = inputStream.getCurrentSentence();
        Phrase[] unFilteredPhrases = inputStream.getCurrentPhrases();
        filterSentence(unFilteredSentence, unFilteredPhrases, vocab);
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
        return inputStream.getWordCount();
    }

}
