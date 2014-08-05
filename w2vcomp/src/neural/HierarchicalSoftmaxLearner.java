package neural;


import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;
import vocab.VocabEntry;

public class HierarchicalSoftmaxLearner extends LearningStrategy{
    protected Vocab vocab;
    
    protected HierarchicalSoftmaxLearner(Vocab vocab, SimpleMatrix outVectors) {
        super(outVectors);
        this.vocab = vocab;
    }
    
 // TODO: random initialization
    public static HierarchicalSoftmaxLearner randomInitialize(Vocab vocab, int outputLayerSize) {
        Random rand = new Random();
        double[][] outVectors = new double[vocab.getVocabSize() -1 ][outputLayerSize];
        for (int i = 0; i < vocab.getVocabSize() -1; i++) {
            for (int j = 0; j < outputLayerSize; j++) {
                outVectors[i][j] = (double) (rand.nextFloat() - 0.5)
                        / outputLayerSize;
            }
        }
        return new HierarchicalSoftmaxLearner(vocab, new SimpleMatrix(outVectors));
    }
    
    // TODO: initialize with zero
    public static HierarchicalSoftmaxLearner zeroInitialize(Vocab vocab, int outputLayerSize) {
        return new HierarchicalSoftmaxLearner(vocab, new SimpleMatrix(vocab.getVocabSize() - 1, outputLayerSize));
    }
    
    // TODO: transpose or not?
    public int[] getOutputIndices(String word) {
        VocabEntry wordEntry = vocab.getEntry(word);
        int[] parentIndices = wordEntry.ancestors;
        return parentIndices;
    }
    
    // TODO: column or row vector?
    public SimpleMatrix getGoldOutput(String word) {
        VocabEntry wordEntry = vocab.getEntry(word);
        String code = wordEntry.code;
        int codeLength = code.length();
        double[] data = new double[codeLength];
        for (int i = 0; i < codeLength; i++) {
            data[i] = code.charAt(i) - 48;
        }
        return new SimpleMatrix(codeLength, 1, false, data);
    }
}
