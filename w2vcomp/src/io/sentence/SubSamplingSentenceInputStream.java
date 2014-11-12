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
    double               frequencyThreshold;
    int[]               sentence;
    Phrase[]            phrases;
    Random              rand = new Random();
    long                realWordCount;

    public SubSamplingSentenceInputStream(SentenceInputStream inputStream,
            double frequencyThreshold) {
        this.inputStream = inputStream;
        this.frequencyThreshold = frequencyThreshold;
        realWordCount = 0;
    }

    protected boolean isSampled(long count, long totalCount) {
        double randomThreshold = (double) (Math.sqrt(count
                / (frequencyThreshold * totalCount)) + 1)
                * (frequencyThreshold * totalCount) / count;
//        if (randomThreshold >= rand.nextFloat()) {
//            return true;
//        } else {
//            return false;
//        }
        if (randomThreshold >= ((rand.nextInt() % 65536) / (double)65536)) {
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
                realWordCount++;
            }
            // set those words'positions that are not in vocab to -1
            else {
                newPositions[i] = Integer.MIN_VALUE;
            }
        }
//        System.out.println("\nOld Sentence:");
//        for (int i = 0; i < unFilteredSentence.length; i++)
//        {
//            System.out.print(" "+unFilteredSentence[i]);
//        }
//        System.out.println("\nOld phrase:");
//        for (int i = 0; i < unFilteredPhrases.length; i++)
//        {
//            System.out.print("("+unFilteredPhrases[i].startPosition + " " + +unFilteredPhrases[i].endPosition + ") ");
//        }
//        System.out.println();
        sentence = DataStructureUtils.intListToArray(filteredIndices);

    }

    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        boolean hasNextSentence = inputStream.readNextSentence(vocab);
        if (hasNextSentence) {
            int[] unFilteredSentence = inputStream.getCurrentSentence();
            Phrase[] unFilteredPhrases = inputStream.getCurrentPhrases();
            filterSentence(unFilteredSentence, unFilteredPhrases, vocab);
        }
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
    
    public long getRealWordCount() {
        return realWordCount;
    }

}
