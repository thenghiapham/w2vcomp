package word2vec;

import io.sentence.SentenceInputStream;
import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import common.DataStructureUtils;

public abstract class SingleThreadWord2Vec extends AbstractWord2Vec {

    protected long oldWordCount;

    public SingleThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, float subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // single-threaded instead of multi-threaded
        oldWordCount = 0;
        wordCount = 0;
        trainWords = vocab.getTrainWords();
        System.out.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        for (SentenceInputStream inputStream : inputStreams) {
            trainModelThread(inputStream);
        }
        System.out.println("total word count: " + wordCount);
    }

    void trainModelThread(SentenceInputStream inputStream) {
        oldWordCount = wordCount;
        long lastWordCount = wordCount;
        try {
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                boolean hasNextSentence = inputStream.readNextSentence(vocab);

                int[] sentence = inputStream.getCurrentSentence();
                Phrase[] phrases = inputStream.getCurrentPhrases();
                // if end of file, finish
                if (sentence.length == 0) {
                    if (!hasNextSentence)
                        break;
                }

                // check word count
                // update alpha
                wordCount = oldWordCount + inputStream.getWordCount();
                if (wordCount - lastWordCount > 10000) {
                    // if (wordCount - lastWordCount > 50) {
                    System.out.println("Trained: " + wordCount + " words");
                    // update alpha
                    // what about thread safe???
                    alpha = starting_alpha
                            * (1 - (float) wordCount / (trainWords + 1));
                    if (alpha < starting_alpha * 0.0001) {
                        alpha = starting_alpha * 0.0001;
                    }
                    System.out.println("Training rate: " + alpha);
                    lastWordCount = wordCount;
                }
                trainSentence(sentence);
                trainPhrases(phrases, sentence);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void trainPhrases(Phrase[] phrases, int[] sentence) {
        for (Phrase phrase : phrases) {
            int[] pseudoSentence = insertPhrase(phrase, sentence);
            trainSinglePhrase(phrase.startPosition, pseudoSentence);
        }
    }

    protected int[] insertPhrase(Phrase phrase, int[] sentence) {
        // TODO: check phrase Position
        // TODO: write a unittest
        // TODO:
        LinkedList<Integer> newSentenceBuffer = new LinkedList<Integer>();

        // System.out.println("phrase: " + phrase.toString());
        // System.out.println("in sentence:\n");
        // IOUtils.printInts(sentence);
        for (int i = 0; i < phrase.startPosition; i++) {
            if (DataStructureUtils.searchSmallIntArray(phrase.componentPositions,
                    i) == -1) {
                newSentenceBuffer.add(sentence[i]);
            }
        }
        newSentenceBuffer.add(phrase.phraseIndex);
        for (int i = phrase.endPosition; i < sentence.length; i++) {
            if (DataStructureUtils.searchSmallIntArray(phrase.componentPositions,
                    i) == -1) {
                newSentenceBuffer.add(sentence[i]);
            }
        }
        // System.out.println("out sentence:\n");
        // IOUtils.printInts(DataStructureUtils.intListToArray(newSentenceBuffer));
        return DataStructureUtils.intListToArray(newSentenceBuffer);
    }

    public abstract void trainSinglePhrase(int phrasePosition,
            int[] pseudoSentence);

    public abstract void trainSentence(int[] sentence);

}
