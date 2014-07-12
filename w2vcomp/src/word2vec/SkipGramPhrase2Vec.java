package word2vec;

//import java.util.HashMap;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;

import io.word.Phrase;
import tree.CcgTree;
import vocab.VocabEntry;

/**
 * Concrete class for single threaded Skip gram
 * @author thenghiapham
 *
 */

public class SkipGramPhrase2Vec extends SingleThreadWord2Vec {
    
//    protected HashMap<Integer, SimpleMatrix> composeMatrices;
    // TODO: adding tanh
    SimpleMatrix typeMatrix;
    double weightDecay = 1e-2;
    
    public SkipGramPhrase2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
    }
    
    public SkipGramPhrase2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample, menFile);
    }
    
    protected void initBare() {
        super.initBare();
        randomInitializeComposeMatrices();
    }
    
    protected void randomInitializeComposeMatrices(){
        double[][] randomMatrix = new double[projectionLayerSize][2 * projectionLayerSize];
        for (int i = 0; i < projectionLayerSize; i++) {
            for (int j = 0; j < 2 * projectionLayerSize; j++) {
                randomMatrix[i][j] = (double) (rand.nextFloat() - 0.5)
                        / projectionLayerSize;
            }
        }
        typeMatrix = new SimpleMatrix(randomMatrix);
    }
    
//    public ini

    public void trainSentence(int[] sentence) {
        // train with the sentence
        // different from cbow, no need for a1
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        for (int wordPosition = 0; wordPosition < sentence.length; wordPosition++) {

            int wordIndex = sentence[wordPosition];

            // no way it will go here
            if (wordIndex == -1)
                continue;

            for (int i = 0; i < projectionLayerSize; i++) {
                a1error[i] = 0;
            }

            // random actual window size
            int start = rand.nextInt(windowSize);

            VocabEntry word = vocab.getEntry(wordIndex);
            for (int i = start; i < windowSize * 2 + 1 - start; i++) {
                if (i != windowSize) {
                    int iPos = wordPosition - windowSize + i;
                    if (iPos < 0 || iPos >= sentenceLength)
                        continue;
                    iWordIndex = sentence[iPos];
                    if (iWordIndex == -1)
                        continue;

                    for (int j = 0; j < projectionLayerSize; j++)
                        a1error[j] = 0;

                    // HIERARCHICAL SOFTMAX
                    if (hierarchicalSoftmax) {
                        for (int bit = 0; bit < word.code.length(); bit++) {
                            double z2 = 0;
                            int iParentIndex = word.ancestors[bit];
                            // Propagate hidden -> output
                            for (int j = 0; j < projectionLayerSize; j++) {
                                z2 += weights0[iWordIndex][j]
                                        * weights1[iParentIndex][j];
                            }

                            double a2 = sigmoidTable.getSigmoid(z2);
                            if (a2 == 0 || a2 == 1)
                                continue;
                            // 'g' is the gradient multiplied by the learning
                            // rate
                            double gradient = (double) ((1 - (word.code
                                    .charAt(bit) - 48) - a2) * alpha);
                            // Propagate errors output -> hidden
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * weights1[iParentIndex][j];
                            }
                            // Learn weights hidden -> output
                            for (int j = 0; j < projectionLayerSize; j++) {
                                weights1[iParentIndex][j] += gradient
                                        * weights0[iWordIndex][j];
                            }
                        }
                    }

                    // NEGATIVE SAMPLING
                    if (negativeSamples > 0) {
                        for (int l = 0; l < negativeSamples + 1; l++) {
                            int target;
                            int label;

                            if (l == 0) {
                                target = wordIndex;
                                label = 1;
                            } else {
                                target = unigram.randomWordIndex();
                                if (target == 0) {
                                    target = rand
                                            .nextInt(vocab.getVocabSize() - 1) + 1;
                                }
                                if (target == wordIndex)
                                    continue;
                                label = 0;
                            }
                            double z2 = 0;
                            double gradient;
                            for (int j = 0; j < projectionLayerSize; j++) {
                                z2 += weights0[iWordIndex][j]
                                        * negativeWeights1[target][j];
                            }
                            double a2 = sigmoidTable.getSigmoid(z2);
                            gradient = (double) ((label - a2) * alpha);
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * negativeWeights1[target][j];
                            }
                            for (int j = 0; j < projectionLayerSize; j++) {
                                negativeWeights1[target][j] += gradient
                                        * weights0[iWordIndex][j];
                            }
                        }
                    }
                    // Learn weights input -> hidden
                    for (int j = 0; j < projectionLayerSize; j++) {
                        weights0[iWordIndex][j] += a1error[j];
                    }
                }

            }

        }

    }

    @Override
    public void trainSinglePhrase(Phrase phrase, int[] sentence) {
     // train with the sentence
        
        
        
        int sentenceLength = sentence.length;
        int iWordIndex = 0;

        // TODO: change this
        int word1Index = ((CcgTree) phrase.tree.getChildren().get(0).getChildren().get(0)).getWordIndex();
        int word2Index = ((CcgTree) phrase.tree.getChildren().get(1).getChildren().get(0)).getWordIndex();
//        System.out.println(word1Index);
//        System.out.println(word2Index);
        
        double[] a0 = new double[2 * projectionLayerSize];
        for (int i = 0; i < projectionLayerSize; i++) {
            a0[i] = weights0[word1Index][i];
            a0[i+projectionLayerSize] = weights0[word2Index][i];
        }
        

        SimpleMatrix mA0 = new SimpleMatrix(new DenseMatrix64F(projectionLayerSize * 2, 1, false, a0));
//        SimpleMatrix typeMatrix = composeMatrices.get(phraseType);
        SimpleMatrix z1 = typeMatrix.mult(mA0);
        

        double[] a1 = z1.getMatrix().data; // for now no activation function 
        double[] a1error = new double[projectionLayerSize]; // all zeros
        
        // random actual window size
        int start = rand.nextInt(windowSize);

        
        for (int iPos = phrase.startPosition - start - 1; iPos <= phrase.endPosition + start + 1; iPos++) {
            if (iPos < 0 || iPos >= sentenceLength || (iPos >= phrase.startPosition && iPos <= phrase.endPosition))
                continue;
            iWordIndex = sentence[iPos];
            if (iWordIndex == -1)
                continue;

            VocabEntry contextWord = vocab.getEntry(iWordIndex);
            // HIERARCHICAL SOFTMAX
            if (hierarchicalSoftmax) {
                for (int bit = 0; bit < contextWord.code.length(); bit++) {
                    double z2 = 0;
                    int iParentIndex = contextWord.ancestors[bit];
                    // Propagate hidden -> output
                    for (int j = 0; j < projectionLayerSize; j++) {
                        z2 += a1[j] * weights1[iParentIndex][j];
                    }

                    double a2 = sigmoidTable.getSigmoid(z2);
                    if (a2 == 0 || a2 == 1)
                        continue;
                    // 'g' is the gradient multiplied by the learning
                    // rate
                    double gradient = (double) ((1 - (contextWord.code
                            .charAt(bit) - 48) - a2) * alpha);
                    // Propagate errors output -> hidden
                    for (int j = 0; j < projectionLayerSize; j++) {
                        a1error[j] += gradient
                                * weights1[iParentIndex][j];
                    }
                    // Learn weights hidden -> output
                    for (int j = 0; j < projectionLayerSize; j++) {
                        weights1[iParentIndex][j] += gradient
                                * a1[j];
                    }
                }
            }

            // NEGATIVE SAMPLING
            if (negativeSamples > 0) {
                for (int l = 0; l < negativeSamples + 1; l++) {
                    int target;
                    int label;

                    if (l == 0) {
                        target = iWordIndex;
                        label = 1;
                    } else {
                        target = unigram.randomWordIndex();
                        if (target == 0) {
                            target = rand
                                    .nextInt(vocab.getVocabSize() - 1) + 1;
                        }
                        if (target == iWordIndex)
                            continue;
                        label = 0;
                    }
                    double z2 = 0;
                    double gradient;
                    for (int j = 0; j < projectionLayerSize; j++) {
                        z2 += a1[j] * negativeWeights1[target][j];
                    }
                    double a2 = sigmoidTable.getSigmoid(z2);
                    gradient = (double) ((label - a2) * alpha);
                    for (int j = 0; j < projectionLayerSize; j++) {
                        a1error[j] += gradient
                                * negativeWeights1[target][j];
                    }
                    for (int j = 0; j < projectionLayerSize; j++) {
                        negativeWeights1[target][j] += gradient
                                * a1[j];
                    }
                }
            }
            // update the composition matrix
            SimpleMatrix mA1Error = new SimpleMatrix(new DenseMatrix64F(projectionLayerSize, 1, true, a1error));
            SimpleMatrix composedMatrixGradient = mA1Error.mult(mA0.transpose());
//            composeMatrices.put(phraseType,typeMatrix.plus(composedMatrixGradient));
            typeMatrix =  typeMatrix.plus(composedMatrixGradient).minus(typeMatrix.scale(weightDecay * alpha));
            // TODO: right formula here
            // Update the input vector
            double[] a0error = typeMatrix.transpose().mult(mA1Error).getMatrix().data; 
            for (int j = 0; j < projectionLayerSize; j++) {
                weights0[iWordIndex][j] += a0error[j];
            }
            
        }
    }
    
    @Override
    public void printStatistics() {
        System.out.println("L2: " + typeMatrix.normF());
    }
    
    public void saveMatrix(String matrixFile, double[][] matrix, boolean binary) {
        // Save the word vectors
        // save number of words, length of each vector
        int numRow = matrix.length;
        int numColumn = matrix[0].length;
        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(matrixFile));
            String firstLine = "" + numRow + " " + numColumn
                    + "\n";
            os.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < matrix.length; i++) {
                if (binary) {
                    ByteBuffer buffer = ByteBuffer
                            .allocate(4 * numColumn);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    for (int j = 0; j < numColumn; j++) {
                        buffer.putFloat((float) matrix[i][j]);
                    }
                    os.write(buffer.array());
                } else {
                    StringBuffer sBuffer = new StringBuffer();
                    for (int j = 0; j < numColumn; j++) {
                        sBuffer.append("" + matrix[i][j] + " ");
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
    
    public void readMatrixFromBinaryFile(String matrixFile) {
        
    }
}
