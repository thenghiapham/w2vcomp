package neural;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.exception.ValueException;

import vocab.Vocab;

/**
 * This class takes care of
 * - keeping vector representation of words
 * - updating vector representation of words
 * - creating projection layers
 * @author thenghiapham
 *
 */
public class ProjectionMatrix {
    // TODO: handling unknown word?
    //       either: learn it, assign zero, or return zero vector? 
    protected HashMap<String, Integer> dictionary;
    protected SimpleMatrix vectors;
    
    protected ProjectionMatrix(Vocab vocab, SimpleMatrix vectors) {
        this.dictionary = new HashMap<>();
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            this.dictionary.put(vocab.getEntry(i).word, i);
        }
        this.vectors = vectors;
    }
    
    
    protected ProjectionMatrix(HashMap<String, Integer> dictionary, SimpleMatrix vectors) {
        this.dictionary = dictionary;
        this.vectors = vectors;
    }
    
    /**
     * 
     * @param vocab
     * @param hiddenLayerSize
     * @return
     */
    public static ProjectionMatrix randomInitialize(Vocab vocab, int hiddenLayerSize) {
        Random rand = new Random();
        double[][] outVectors = new double[vocab.getVocabSize() + 1][hiddenLayerSize];
        for (int i = 0; i < vocab.getVocabSize() + 1; i++) {
            for (int j = 0; j < hiddenLayerSize; j++) {
                outVectors[i][j] = (double) (rand.nextFloat() - 0.5)
                        / hiddenLayerSize;
            }
        }
        return new ProjectionMatrix(vocab, new SimpleMatrix(outVectors));
    }
    
    // TODO: initialize with zero
    public static ProjectionMatrix zeroInitialize(Vocab vocab, int hiddenLayerSize) {
        return new ProjectionMatrix(vocab, new SimpleMatrix(vocab.getVocabSize() + 1, hiddenLayerSize));
    }
    
    // TODO: initialize with saved matrix
    public static ProjectionMatrix initializeFromMatrix(Vocab vocab, SimpleMatrix saveMatrix) {
        if (vocab.getVocabSize() != saveMatrix.numRows() - 1)
            throw new ValueException("the matrix should have one column for unknown world");
        return new ProjectionMatrix(vocab, saveMatrix);
    }
    
    public SimpleMatrix getVector(String word) {
        
        int wordIndex = getWordIndex(word);
        // TODO: transpose all?
        // what about unknown word here
        return getVector(wordIndex); 
    }
    
    public SimpleMatrix getVector(int wordIndex) {
        // TODO: null or zeros?
        SimpleMatrix result = null;
        if (wordIndex <= -2 || wordIndex >= dictionary.size())
            return result;
        else if (wordIndex == -1)
            result = vectors.extractVector(true, dictionary.size());
        else
            result = vectors.extractVector(true, wordIndex);
        return result.transpose();
    }
    
    public int getWordIndex(String word) {
        if (dictionary.containsKey(word))
            return dictionary.get(word);
        else
            return -1;
    }
    
    // synchronize??
    // maybe not as neccessary as synchronizing the composition matrix
    protected void updateVector(int wordIndex, SimpleMatrix gradient, 
            double learningRate) {

//        String word;
        if (wordIndex == -1) {
//            word = "default";
            wordIndex = dictionary.size();
        } else {
//            word = vocab.getEntry(wordIndex).word;
        }
        
        SimpleMatrix originalRow = vectors.extractVector(true, wordIndex);
        if (gradient == null) {
//            System.out.println(word + " null");
            return;
        } else {
            gradient = gradient.transpose();
        }
//        System.out.println(word + " not_null");
        gradient = gradient.scale(learningRate);
        SimpleMatrix newRow = originalRow.minus(gradient);
        vectors.setRow(wordIndex, 0, newRow.getMatrix().getData());
    
    }
    
    //TODO: copy the matrix so that it won't be modified?
    public SimpleMatrix getMatrix() {
        return vectors;
    }
    
    public static ProjectionMatrix loadProjectionMatrix(BufferedInputStream inputStream, boolean binary) throws IOException{
        // TODO: binary
        int wordNumber = Integer.parseInt(IOUtils.readWord(inputStream));
        int vectorSize = Integer.parseInt(IOUtils.readWord(inputStream));
        byte[] rowData = new byte[vectorSize * 4];
        ByteBuffer buffer = ByteBuffer.wrap(rowData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        HashMap<String, Integer> vocab = new HashMap<>();
        // TODO: add unknown word?
        SimpleMatrix vectors = new SimpleMatrix(wordNumber, vectorSize);
        for (int i = 0; i < wordNumber; i++) {
            String word = IOUtils.readWord(inputStream);
            inputStream.read(rowData);
            for (int j = 0; j < vectorSize; j++) {
                vectors.set(i, j, buffer.getFloat(j * 4));
            }
            vocab.put(word, i);
            inputStream.read();
        }
        return new ProjectionMatrix(vocab, vectors);
    }
    
    public int getVectorSize() {
        return vectors.numCols();
    }
}
