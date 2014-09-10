package word2vec;

import io.sentence.TreeInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

import neural.CompositionMatrices;
import neural.HierarchicalSoftmaxLearner;
import neural.LearningStrategy;
import neural.NegativeSamplingLearner;
import neural.ProjectionMatrix;

import vocab.Vocab;
import vocab.VocabEntry;
import common.IOUtils;

/**
 * Abstract class of word2vec
 * Define common attributes and methods for learning word vectors
 * @author thenghiapham
 *
 */

public abstract class Sentence2Vec {
    public static final double DEFAULT_STARTING_ALPHA      = 0.025;
    public static final int    DEFAULT_MAX_SENTENCE_LENGTH = 100;

    protected double           starting_alpha;
    protected double           alpha;

    /*
     * objective function options: - hierarchicalSoftmax:
     * log(p(output_word_code|input_words)) - negativeSampling
     */
    protected boolean          hierarchicalSoftmax;
    protected int              negativeSamples;

    // TODO: Keep this to throw away the context
    protected double            subSample;

    // projection/hidden layer size
    protected int              hiddenLayerSize;
    protected int              windowSize;
    protected Vocab            vocab;

    // parameters to keep track of the training progress
    protected long             totalLines;
    protected long             trainedLines;
    
    protected HashMap<String, String> constructionGroups;
    
    protected ProjectionMatrix      projectionMatrix;
    protected CompositionMatrices   compositionMatrices;
    protected LearningStrategy      learningStrategy;
    
    protected int phraseHeight;
    boolean allLevel;
    boolean lexical;


    public Sentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, 
            HashMap<String, String> constructionGroups, int phraseHeight, 
            boolean allLevel, boolean lexical) {
        this.hiddenLayerSize = hiddenLayerSize;
        this.hierarchicalSoftmax = hierarchicalSoftmax;
        this.windowSize = windowSize;
        this.subSample = subSample;
        this.negativeSamples = negativeSamples;
        this.phraseHeight = phraseHeight;
        this.allLevel = allLevel;
        this.lexical = lexical;
        this.constructionGroups = constructionGroups;

        // TODO: setting alpha
        starting_alpha = DEFAULT_STARTING_ALPHA;
        alpha = starting_alpha;
    }
    
    public void setVocab(Vocab vocab) {
        this.vocab = vocab;
    }
    
    // call after learning or loading vocabulary
    public void initNetwork() {
        projectionMatrix = ProjectionMatrix.randomInitialize(vocab, hiddenLayerSize);
        if (hierarchicalSoftmax) {
            learningStrategy = HierarchicalSoftmaxLearner.zeroInitialize(vocab, hiddenLayerSize);
        } else {
            learningStrategy = NegativeSamplingLearner.zeroInitialize(vocab, negativeSamples, hiddenLayerSize);
        }
        compositionMatrices = CompositionMatrices.identityInitialize(constructionGroups, hiddenLayerSize);
        vocab.assignCode();
        
        // number of lines = frequency of end of line character?
        this.totalLines = vocab.getEntry(0).frequency;
    }

    public void initNetwork(String initFile) {
        loadNetwork(initFile, true);
    }


    public void saveVector(String outputFile, boolean binary) {
        // Save the word vectors as a Semantic space
        // save number of words, length of each vector
        int vocabSize = vocab.getVocabSize();

        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(outputFile));
            String firstLine = "" + vocabSize + " " + hiddenLayerSize
                    + "\n";
            os.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < vocabSize; i++) {
                VocabEntry word = vocab.getEntry(i);
                double[] vector = projectionMatrix.getVector(i).getMatrix().getData();
                os.write((word.word + " ").getBytes("UTF-8"));
                if (binary) {
                    ByteBuffer buffer = ByteBuffer
                            .allocate(4 * hiddenLayerSize);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    for (int j = 0; j < hiddenLayerSize; j++) {
                        buffer.putFloat((float) vector[j]);
                    }
                    os.write(buffer.array());
                } else {
                    StringBuffer sBuffer = new StringBuffer();
                    for (int j = 0; j < hiddenLayerSize; j++) {
                        sBuffer.append("" + vector[j] + " ");
                    }
                    os.write(sBuffer.toString().getBytes());
                }
                os.write("\n".getBytes());
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    
    // TODO: composition matrices?
    public void saveNetwork(String outputFile, boolean binary) {
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            IOUtils.saveMatrix(outputStream, projectionMatrix.getMatrix(), binary);
            IOUtils.saveMatrix(outputStream, learningStrategy.getMatrix(), binary);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public void loadNetwork(String inputFile, boolean binary) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            double[][] matrix = IOUtils.readMatrix(inputStream, binary);
            if (matrix.length != vocab.getVocabSize() || matrix[0].length != hiddenLayerSize) {
                System.out.println("matrix size does not match");
            } else {
                projectionMatrix = ProjectionMatrix.initializeFromMatrix(vocab, new SimpleMatrix(matrix));
            }
            
            if (hierarchicalSoftmax) {
                matrix = IOUtils.readMatrix(inputStream, binary);
                if (matrix.length != vocab.getVocabSize() -1 || matrix[0].length != hiddenLayerSize) {
                    System.out.println("matrix size does not match");
                } else {
                    learningStrategy = HierarchicalSoftmaxLearner.initializeFromMatrix(vocab, new SimpleMatrix(matrix));
                }
            } else {
                matrix = IOUtils.readMatrix(inputStream, binary);
                if (matrix.length != vocab.getVocabSize() || matrix[0].length != hiddenLayerSize) {
                    System.out.println("matrix size does not match");
                } else {
                    learningStrategy = NegativeSamplingLearner.initializeFromMatrix(vocab, negativeSamples, new SimpleMatrix(matrix));
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void trainModel(ArrayList<TreeInputStream> inputStreams);

}
