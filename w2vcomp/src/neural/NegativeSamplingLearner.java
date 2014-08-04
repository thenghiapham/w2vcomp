package neural;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import vocab.Vocab;
import word2vec.UniGram;

public class NegativeSamplingLearner {
    protected SimpleMatrix outputVectors;
    protected UniGram uniGram;
    protected Vocab vocab;
    protected int noSamples;
    
    protected SimpleMatrix goldOutput;
    
    protected Random rand;
    
    // TODO: initialization
    public NegativeSamplingLearner(Vocab vocab, int noSamples) {
        this.vocab = vocab;
        this.noSamples = noSamples;
        this.uniGram = new UniGram(vocab);
        initializeOutput();
    }
    
    protected void initializeOutput() {
        double[] data = new double[noSamples + 1];
        data[0] = 1;
        goldOutput = new SimpleMatrix(noSamples + 1, 1, false, data);
    }
    
    public SimpleMatrix getOutputWeights(String word) {
        int wordIndex = vocab.getWordIndex(word);
        
        int[] sampleIndices = new int[noSamples + 1];
        sampleIndices[0] = wordIndex;
        
        for (int i = 0; i < noSamples; i++) {
            int sampleWordIndex = uniGram.randomWordIndex();
            while (sampleWordIndex == wordIndex) {
                sampleWordIndex = rand.nextInt(vocab.getVocabSize());
            }
            sampleIndices[i + 1] = sampleWordIndex;
        }
        return SimpleMatrixUtils.getRows(outputVectors, sampleIndices);
    }
    
    public SimpleMatrix getGoldOutput(String word) {
        return goldOutput;
    }
}
