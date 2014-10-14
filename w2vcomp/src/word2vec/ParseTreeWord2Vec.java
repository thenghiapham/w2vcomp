package word2vec;

import io.sentence.SentenceInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.correlation.MenCorrelation;


//import neural.IdentityFunction;

import space.RawSemanticSpace;
import vocab.VocabEntry;

public class ParseTreeWord2Vec extends AbstractWord2Vec{
    private static final Logger LOGGER = Logger.getLogger(ParseTreeWord2Vec.class.getName());
    // parameters to keep track of the training progress
    protected long             totalLines;
    protected long             trainedLines;
    MenCorrelation men;
    protected RawSemanticSpace outputSpace;

    public ParseTreeWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
    }
    
    public ParseTreeWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
        men = new MenCorrelation(menFile);
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // TODO Auto-generated method stub
        this.totalLines = vocab.getEntry(0).frequency;
        System.out.println("line num: " + totalLines);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        
        if (men != null) {
            outputSpace = new RawSemanticSpace(vocab, weights0, false);
        }
        
        for (SentenceInputStream inputStream : inputStreams) {
            trainModelThread(inputStream);
        }
        System.out.println("total read line num: " + this.trainedLines);
    }

    protected void trainModelThread(SentenceInputStream inputStream) {
        // number of trained lines before this stream
        printStatistics();
        long tmpTrainedLines = trainedLines;
        try {
            int iteration = 0;
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                if (!inputStream.readNextSentence(vocab)) break;
                int[] sentence = inputStream.getCurrentSentence();
                if (sentence == null) {
                    break;
                } 

                // check word count
                // update alpha
                trainedLines++;
                
                if (trainedLines - tmpTrainedLines >= 1000) {
                    iteration++;
                    // update alpha
                    // what about thread safe???
                    alpha = starting_alpha
                            * (1 - (double) trainedLines / (totalLines + 1));
                    if (alpha < starting_alpha * 0.0001) {
                        alpha = starting_alpha * 0.0001;
                    }
                    LOGGER.log(Level.INFO, "Trained: " + trainedLines + " lines");
                    LOGGER.log(Level.INFO, "Training rate: " + alpha);
                    tmpTrainedLines = trainedLines;
                    if (iteration % 4 == 0) {
                        printStatistics();
                    }
                }
                
                trainSentence(sentence);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected void printStatistics() {
        // TODO Auto-generated method stub
        if (men != null) {
//            LOGGER.log(Level.INFO, "men: " + men.evaluateSpacePearson(new SMSemanticSpace(vocab, vec, false)));
            System.out.println("men: " + men.evaluateSpacePearson(outputSpace));
            System.out.println("alpha: " + alpha);
        }
    }

    public void trainSentence(int[] sentence) {
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        for (int wordPosition = 0; wordPosition < sentence.length; wordPosition++) {

            int wordIndex = sentence[wordPosition];

            // no way it will go here
            if (wordIndex == -1)
                continue;

            for (int i = 0; i < projectionLayerSize; i++) {
                a1[i] = 0;
                a1error[i] = 0;
            }

            // random actual window size
            int start = rand.nextInt(windowSize);
            // TODO: turn it back to random
//            int start = 0;

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
//                            System.out.print(" " + iParentIndex);
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
//                        System.out.println();
//                        System.out.println("****");
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
//        System.exit(0);

    }


    
}
