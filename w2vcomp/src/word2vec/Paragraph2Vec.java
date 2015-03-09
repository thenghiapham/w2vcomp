package word2vec;

import io.sentence.SentenceInputStream;

import java.util.ArrayList;

import vocab.Vocab;
import vocab.VocabEntry;

import common.DataStructureUtils;
import common.exception.ValueException;

public abstract class Paragraph2Vec extends AbstractWord2Vec{
    protected double[][] paragraphVectors;
    protected int iterationNum;
    
    public Paragraph2Vec(String networkFile, String vocabFile, int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int negativeSamplesImages, double subSample, int iterationNum) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples, negativeSamplesImages,
                subSample);
        vocab = new Vocab();
        vocab.loadVocab(vocabFile);
        
        loadNetwork(networkFile, true);
        this.iterationNum = iterationNum;
    }

    public void trainParagraphVector(String[] sentences) {
        if (hierarchicalSoftmax) {
            vocab.assignCode();
        }
        if (negativeSamples > 0) {
            unigram = new UniGram(vocab);
        }
        int sentenceNum = sentences.length;
        paragraphVectors = new double[sentenceNum][projectionLayerSize];
        this.alpha = DEFAULT_STARTING_ALPHA;
        for (int iteration = 0; iteration < iterationNum; iteration++) {
            alpha = alpha * (iterationNum - iteration) / iterationNum;
            for (int sentenceIndex = 0; sentenceIndex < sentences.length; sentenceIndex++) {
                String sentence = sentences[sentenceIndex];
                trainSentence(sentenceIndex,sentence);
            }
        }
    }
    
    protected void trainSentence(int sentenceIndex, String sentence) {
        String[] words = sentence.split("( |\t)");
        ArrayList<Integer> wordIndices = new ArrayList<>();
        long totalCount = vocab.getTrainWords();
        for (String word: words) {
            int wordIndex = vocab.getWordIndex(word);
            if (wordIndex != -1) {
                if (subSample > 0) {
                    VocabEntry entry = vocab.getEntry(wordIndex);
                    long count = entry.frequency;
                    if (!isSampled(count, totalCount)) {
                        continue;
                    }
                }
                wordIndices.add(wordIndex);
            }
        }
        int[] wordIndexArray = DataStructureUtils.intListToArray(wordIndices);
        trainSentence(sentenceIndex, wordIndexArray);
    }
    
    protected abstract void trainSentence(int sentenceIndex, int[] wordIndexArray);

    protected boolean isSampled(long count, long totalCount) {
        double randomThreshold = (double) (Math.sqrt(count
                / (subSample * totalCount)) + 1)
                * (subSample * totalCount) / count;
        if (randomThreshold >= rand.nextFloat()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // TODO Auto-generated method stub
        throw new ValueException("Not implemented");
        
    }
    
    public double[][] getParagraphVectors() {
        return paragraphVectors;
    }
    
}