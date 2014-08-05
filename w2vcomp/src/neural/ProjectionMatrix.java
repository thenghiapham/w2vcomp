package neural;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import vocab.Vocab;

public class ProjectionMatrix {
    protected Vocab vocab;
    protected SimpleMatrix vectors;
    
    protected ProjectionMatrix(Vocab vocab, SimpleMatrix vectors) {
        this.vocab = vocab; 
        this.vectors = vectors;
    }
    
    // TODO: random initialization
    public static ProjectionMatrix randomInitialize(Vocab vocab, int hiddenLayerSize) {
        Random rand = new Random();
        double[][] outVectors = new double[vocab.getVocabSize()][hiddenLayerSize];
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            for (int j = 0; j < hiddenLayerSize; j++) {
                outVectors[i][j] = (double) (rand.nextFloat() - 0.5)
                        / hiddenLayerSize;
            }
        }
        return new ProjectionMatrix(vocab, new SimpleMatrix(outVectors));
    }
    
    // TODO: initialize with zero
    public static ProjectionMatrix zeroInitialize(Vocab vocab, int hiddenLayerSize) {
        return new ProjectionMatrix(vocab, new SimpleMatrix(vocab.getVocabSize(), hiddenLayerSize));
    }
    
    // TODO: initialize with saved matrix
    public static ProjectionMatrix initializeFromMatrix(Vocab vocab, SimpleMatrix saveMatrix) {
        return new ProjectionMatrix(vocab, saveMatrix);
    }
    
    public SimpleMatrix getVector(String word) {
        int wordIndex = vocab.getWordIndex(word);
        return getVector(wordIndex); 
    }
    
    // TODO: 
    public SimpleMatrix getVectors(String[] words) {
        int[] indices = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            indices[i] = vocab.getWordIndex(words[i]);
        }
        return SimpleMatrixUtils.getRows(vectors, indices);
    }
    

    public SimpleMatrix getVector(int wordIndex) {
        // TODO: null or zeros?
        if (wordIndex <= -1 || wordIndex >= vocab.getVocabSize())
            return null;
        else
            return vectors.extractVector(true, wordIndex);
    }
    
    // synchronize??
    // maybe not as neccessary as synchronizing the composition matrix
    protected void updateVector(int wordIndex, SimpleMatrix gradient, 
            double learningRate) {
        SimpleMatrix originalRow = vectors.extractVector(true, wordIndex);
        SimpleMatrix newRow = originalRow.plus(gradient.scale(learningRate));
        vectors.setRow(wordIndex, 0, newRow.getMatrix().getData());
    
    }
}
