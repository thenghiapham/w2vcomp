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
    //       either: assign zero, don't update? 
    // for now, zero vector, don't use unknow word
    protected HashMap<String, Integer> dictionary;
    protected double[][] vectors;
    protected SimpleMatrix zeroVector;
    int vectorSize;
    
    /**
     * protected constructors, forcing users to use the static methods to 
     * instantiate an object
     * @param vocab
     * @param vectors
     */
    protected ProjectionMatrix(Vocab vocab, double[][] vectors) {
        this.dictionary = new HashMap<>();
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            this.dictionary.put(vocab.getEntry(i).word, i);
        }
        this.vectors = vectors;
        vectorSize = vectors[0].length;
        zeroVector = new SimpleMatrix(vectorSize, 1);
    }
    
    
    protected ProjectionMatrix(HashMap<String, Integer> dictionary, double[][] vectors) {
        this.dictionary = dictionary;
        this.vectors = vectors;
        vectorSize = vectors[0].length;
        zeroVector = new SimpleMatrix(vectorSize, 1);
    }
    
    /**
     * create a projection matrix with randomly initialized the vectors
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
        return new ProjectionMatrix(vocab, outVectors);
    }
    
    /**
     * create a projection matrix with the vectors initialized as zero
     * @param vocab
     * @param hiddenLayerSize
     * @return
     */
    public static ProjectionMatrix zeroInitialize(Vocab vocab, int hiddenLayerSize) {
        return new ProjectionMatrix(vocab, new double[vocab.getVocabSize() + 1][ hiddenLayerSize]);
    }
    
    /**
     * create a projection matrix with saved vectors 
     * @param vocab
     * @param saveMatrix
     * @return
     */
    public static ProjectionMatrix initializeFromMatrix(Vocab vocab, double[][] saveMatrix) {
        if (vocab.getVocabSize() != saveMatrix.length)
            throw new ValueException("the matrix should have one column for unknown word");
        return new ProjectionMatrix(vocab, saveMatrix);
    }
    
    
    /**METHODS FOR EXTRACTING A VECTOR**/
    
    /**
     * Extracting a vector for a word in the vocabulary
     * If the word is not in the vocabulary, return the zero vector
     * @param word
     * @return
     */
    public SimpleMatrix getVector(String word) {
        
        int wordIndex = getWordIndex(word);
        return getVector(wordIndex); 
    }
    
    
    /**
     * Extracting a vector for a word using the word index
     * If the word index is -1 return zero vector
     * @param word
     * @return
     */
    public SimpleMatrix getVector(int wordIndex) {
        // TODO: null or zeros?
        SimpleMatrix result = null;
        if (wordIndex >= dictionary.size())
            return result;
        else if (wordIndex == -1)
            result = zeroVector;
        else
            result = new SimpleMatrix(vectorSize, 1, true, vectors[wordIndex]);
        return result;
    }
    
    /**
     * Get the index in the vocabulary of a word
     * return -1 if the word is not in the vocabulary
     * @param word
     * @return
     */
    public int getWordIndex(String word) {
        if (dictionary.containsKey(word))
            return dictionary.get(word);
        else
            return -1;
    }
    
    // TODO: considering synchronizing for parallelizing
    /**
     * Update the vector for a word (using its index) with a gradient & a learning rate
     * (it is called while learning with compomik)
     * @param wordIndex
     * @param gradient
     * @param learningRate
     */
    protected void updateVector(int wordIndex, SimpleMatrix gradient, 
            double learningRate) {
        if (wordIndex == -1) {
            // don't update the zero vector
            return;
        }
        
        if (gradient == null) {
            throw new ValueException("Gradient cannot be null");
        } else {
            gradient = gradient.transpose();
        }
        // scale with learning rate
        // subtract the vector with the gradient
        // assign back the vector
        gradient = gradient.scale(learningRate);
        double[] gradData = gradient.getMatrix().data;
        for (int i = 0; i < vectorSize; i++) {
            vectors[wordIndex][i] -= gradData[i];
        }
    }
    
    
    /**
     * Load a ProjectionMatrix instance from a file
     * The data in a file should have this format
     * - number of words (string), vector size (string)
     * - [word (string), vector (list of float in binary), endline]
     * @param inputStream
     * @param binary
     * @return
     * @throws IOException
     */
    public static ProjectionMatrix loadProjectionMatrix(BufferedInputStream inputStream, boolean binary) throws IOException{
        // TODO: implement the case where binary = false
        // for now, only consider binary = true
        
        // read the number of words & vector size
        int wordNumber = Integer.parseInt(IOUtils.readWord(inputStream));
        int vectorSize = Integer.parseInt(IOUtils.readWord(inputStream));
        
        // read every word and its corresponding vector
        byte[] rowData = new byte[vectorSize * 4];
        ByteBuffer buffer = ByteBuffer.wrap(rowData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        HashMap<String, Integer> dictionary = new HashMap<>();
        double[][] vectors = new double[wordNumber][vectorSize];
        for (int i = 0; i < wordNumber; i++) {
            String word = IOUtils.readWord(inputStream);
            inputStream.read(rowData);
            for (int j = 0; j < vectorSize; j++) {
                vectors[i][j] = buffer.getFloat(j * 4);
            }
            dictionary.put(word, i);
            inputStream.read();
        }
        return new ProjectionMatrix(dictionary, vectors);
    }
    
    
    /**GET SET METHODS**/
    public double[][] getMatrix() {
        return vectors;
    }
    
    public int getVectorSize() {
        return vectorSize;
    }
    
}
