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
        boolean[] chosen = new boolean[unFilteredSentence.length];
        int newPosition = 0;
        for (int i = 0; i < unFilteredSentence.length; i++) {
            int vocabEntryIndex = unFilteredSentence[i];
            VocabEntry entry = vocab.getEntry(vocabEntryIndex);
            long count = entry.frequency;

            if (isSampled(count, totalCount)) {
                filteredIndices.add(vocabEntryIndex);
                newPositions[i] = newPosition;
                newPosition++;
                chosen[i] = true;
            }
            // set those words'positions that are not in vocab to -1
            else {
                newPositions[i] = newPosition;
                chosen[i] = false;
            }
        }
        sentence = DataStructureUtils.intListToArray(filteredIndices);

        ArrayList<Phrase> fileterPhraseList = new ArrayList<Phrase>();
        for (Phrase unFilteredPhrase : unFilteredPhrases) {
            int phraseIndex = unFilteredPhrase.phraseIndex;
            VocabEntry entry = vocab.getEntry(phraseIndex);
            long count = entry.frequency;
            if (isSampled(count, totalCount)) {
                int startPosition = newPositions[unFilteredPhrase.startPosition];
                int endPosition = newPositions[unFilteredPhrase.endPosition];
                int[] oldComponentPosition = unFilteredPhrase.componentPositions;
                int[] componentPositions = new int[oldComponentPosition.length];
                for (int i = 0; i < componentPositions.length; i++) {
                    if (oldComponentPosition[i] == -1) {
                        componentPositions[i] = -1;
                    } else if (chosen[oldComponentPosition[i]]) {
                        componentPositions[i] = newPositions[componentPositions[i]];
                    } else {
                        componentPositions[i] = -1;
                    }
                }
                Phrase phrase = new Phrase(phraseIndex, startPosition,
                        endPosition, componentPositions);
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
