package neural;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;
import word2vec.UniGram;

public class NegativeSamplingLearner extends LearningStrategy{
    protected UniGram uniGram;
    protected Vocab vocab;
    protected int noSamples;
    protected CostFunction costFunction;
    
    protected SimpleMatrix goldOutput;
    
    protected Random rand;
    
    
    // TODO: random initialization
    public static NegativeSamplingLearner randomInitialize(Vocab vocab, int noSamples, int outputLayerSize) {
        Random rand = new Random();
        double[][] outVectors = new double[vocab.getVocabSize()][outputLayerSize];
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            for (int j = 0; j < outputLayerSize; j++) {
                outVectors[i][j] = (double) (rand.nextFloat() - 0.5)
                        / outputLayerSize;
            }
        }
        return new NegativeSamplingLearner(vocab, new SimpleMatrix(outVectors), noSamples);
    }
    
    // TODO: initialize with zero
    public static NegativeSamplingLearner zeroInitialize(Vocab vocab, int noSamples, int outputLayerSize) {
        return new NegativeSamplingLearner(vocab, new SimpleMatrix(vocab.getVocabSize(), outputLayerSize), noSamples);
    }
    
 // TODO: initialize with saved matrix
    public static NegativeSamplingLearner initializeFromMatrix(Vocab vocab,  int noSamples, SimpleMatrix saveMatrix) {
        return new NegativeSamplingLearner(vocab, saveMatrix, noSamples);
    }
    
    protected NegativeSamplingLearner(Vocab vocab, SimpleMatrix outVectors, int noSamples) {
        super(outVectors);
        
        this.vocab = vocab;
        this.noSamples = noSamples;
        this.uniGram = new UniGram(vocab);
        costFunction = new NegativeSamplingCost();
        
        initializeOutput();
    }
    
    protected void initializeOutput() {
        double[] data = new double[noSamples + 1];
        data[0] = 1;
        goldOutput = new SimpleMatrix(noSamples + 1, 1, false, data);
    }
    
    public int[] getOutputIndices(String word) {
        
        int wordIndex = vocab.getWordIndex(word);
        if (wordIndex == -1) return null;
        int[] sampleIndices = new int[noSamples + 1];
        sampleIndices[0] = wordIndex;
        
        for (int i = 0; i < noSamples; i++) {
            int sampleWordIndex = uniGram.randomWordIndex();
            while (sampleWordIndex == wordIndex) {
                sampleWordIndex = rand.nextInt(vocab.getVocabSize());
            }
            sampleIndices[i + 1] = sampleWordIndex;
        }
        return sampleIndices;
    }
    
    public SimpleMatrix getGoldOutput(String word) {
        return goldOutput;
    }

    @Override
    public CostFunction getCostFunction() {
        // TODO Auto-generated method stub
        return costFunction;
    }
}
