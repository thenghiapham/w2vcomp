package neural;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import vocab.Vocab;

public class ProjectionMatrix {
    protected Vocab vocab;
    protected SimpleMatrix vectors;
    protected ProjectionMatrix(Vocab vocab) {
        this.vocab = vocab; 
    }
    
    protected SimpleMatrix getVector(String word) {
        int wordIndex = vocab.getWordIndex(word);
        return getVector(wordIndex); 
    }
    
    // TODO: 
    protected SimpleMatrix getVectors(String[] words) {
        int[] indices = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            indices[i] = vocab.getWordIndex(words[i]);
        }
        return SimpleMatrixUtils.getRows(vectors, indices);
    }
    

    protected SimpleMatrix getVector(int wordIndex) {
        // TODO: null or zeros?
        if (wordIndex <= -1 || wordIndex >= vocab.getVocabSize())
            return null;
        else
            return vectors.extractVector(true, wordIndex);
    }
    
    protected void updateVector(int wordIndex, SimpleMatrix gradient, 
            double learningRate) {
        SimpleMatrix originalRow = vectors.extractVector(true, wordIndex);
        SimpleMatrix newRow = originalRow.plus(gradient.scale(learningRate));
        vectors.setRow(wordIndex, 0, newRow.getMatrix().getData());
    
    }
    
//    protected void updateVectors(int[] wordIndices, SimpleMatrix gradients,
//            double learningRate) {
//        
//    }
}
