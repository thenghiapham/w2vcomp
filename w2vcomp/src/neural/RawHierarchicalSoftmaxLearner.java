package neural;


import java.util.Random;

import neural.function.HierarchicalSoftmaxObjective2;
import neural.function.ObjectiveFunction;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import vocab.Vocab;
import vocab.VocabEntry;

public class RawHierarchicalSoftmaxLearner extends LearningStrategy{
    protected Vocab vocab;
    protected ObjectiveFunction costFunction;
    protected double[][] rawMatrix;
    
    protected RawHierarchicalSoftmaxLearner(Vocab vocab, SimpleMatrix outVectors) {
        super(outVectors);
        this.vocab = vocab;
        costFunction = new HierarchicalSoftmaxObjective2();
        this.outputVectors = null;
        this.rawMatrix = SimpleMatrixUtils.to2DArray(outVectors);
    }
    
    // TODO: random initialization
    public static RawHierarchicalSoftmaxLearner randomInitialize(Vocab vocab, int outputLayerSize) {
        Random rand = new Random();
        double[][] outVectors = new double[vocab.getVocabSize() -1 ][outputLayerSize];
        for (int i = 0; i < vocab.getVocabSize() -1; i++) {
            for (int j = 0; j < outputLayerSize; j++) {
                outVectors[i][j] = (double) (rand.nextFloat() - 0.5)
                        / outputLayerSize;
            }
        }
        return new RawHierarchicalSoftmaxLearner(vocab, new SimpleMatrix(outVectors));
    }
    
    // TODO: initialize with zero
    public static RawHierarchicalSoftmaxLearner zeroInitialize(Vocab vocab, int outputLayerSize) {
        return new RawHierarchicalSoftmaxLearner(vocab, new SimpleMatrix(vocab.getVocabSize() - 1, outputLayerSize));
    }
    
    // TODO: initialize with saved matrix
    public static RawHierarchicalSoftmaxLearner initializeFromMatrix(Vocab vocab, SimpleMatrix saveMatrix) {
        return new RawHierarchicalSoftmaxLearner(vocab, saveMatrix);
    }
    
    
    // TODO: transpose or not?
    public int[] getOutputIndices(String word) {
        // TODO: return null if out of vocabulary
        if (vocab.getWordIndex(word) == -1) return null;
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

    @Override
    public ObjectiveFunction getCostFunction() {
        // TODO Auto-generated method stub
        return costFunction;
    }
    
    @Override
    public SimpleMatrix getOutputWeights(int[] indices) {
        // TODO: transpose all?
        double[][] result = new double[indices.length][];
        for (int i = 0; i < indices.length; i++) {
            result[i] = rawMatrix[indices[i]];
        }
        return new SimpleMatrix(result);
    }
    
    @Override
    public void updateMatrix(int[] indices, SimpleMatrix gradients, double learningRate) {
        gradients = gradients.scale(learningRate);
        int vectorSize = gradients.numCols();
        int rowStartIndex = 0;
        double[] rawGradients = gradients.getMatrix().data;
        for (int i = 0; i < indices.length; i++) {
            for (int j = 0; j < vectorSize; j++) {
                rawMatrix[indices[i]][j] -= rawGradients[rowStartIndex + j];
            }
            rowStartIndex += vectorSize;
        }
    }
    
    
    @Override
    public SimpleMatrix getMatrix() {
        return new SimpleMatrix(rawMatrix);
    }
}
