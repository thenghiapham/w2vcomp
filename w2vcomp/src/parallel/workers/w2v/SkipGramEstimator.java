package parallel.workers.w2v;

import io.sentence.SentenceInputStream;
import io.sentence.SubSamplingSentenceInputStream;
import io.word.Phrase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import common.MathUtils;
import common.SigmoidTable;
import common.correlation.MenCorrelation;
import demo.TestConstants;

import parallel.comm.ParameterMessager;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterEstimator;
import space.RawSemanticSpace;
import tree.Tree;
import vocab.Vocab;
import vocab.VocabEntry;
import word2vec.UniGram;

public class SkipGramEstimator implements ParameterEstimator{
    Vocab vocab;
    double alpha;
    
    boolean          hierarchicalSoftmax;
    int              negativeSamples;

    double            subSample;

    int              projectionLayerSize;
    int              windowSize;
    
    protected UniGram          unigram;
    protected SigmoidTable     sigmoidTable;

    double[][]                  weights0, weights1;
    double[][]                  oldWeights0, oldWeights1;
    
    SentenceInputStream inputStream;
    
    Random rand = new Random();
    MenCorrelation men;
    
    public SkipGramEstimator(SentenceInputStream inputStream) {
        this.inputStream = inputStream;
        this.hierarchicalSoftmax = RunningConstant.HIERARCHICAL_SOFTMAX;
        this.negativeSamples = RunningConstant.NEGATIVE_SAMPLES;
        this.subSample = RunningConstant.SUBSAMPLE;
        this.projectionLayerSize = RunningConstant.VECTOR_SIZE;
        this.windowSize = RunningConstant.WINDOW_SIZE;
        if (subSample > 0) {
            inputStream = new SubSamplingSentenceInputStream(inputStream, subSample);
        }
        this.vocab = new Vocab(RunningConstant.MIN_FREQUENCY);
        this.sigmoidTable = new SigmoidTable();
        men = new MenCorrelation(TestConstants.S_MEN_FILE);
        System.out.println("Men size: " + men.getGolds().length);
    }
    
    
    
    @Override
    public void run(ModelParameters init, ParameterMessager parameterMessager) {
        vocab.loadVocab(TestConstants.S_VOCABULARY_FILE);
        if (negativeSamples > 0) {
            this.unigram = new UniGram(vocab);
        }
        vocab.assignCode();
        System.out.println("Dictionary size :" + vocab.getVocabSize());
        System.out.println("2nd word :" + vocab.getEntry(2).word);
        
        SkipGramParameters modelParams = (SkipGramParameters) init;
        alpha = modelParams.alpha;
        SkipGramParameters deltaParams;
        weights0 = modelParams.weights0;
        weights1 = modelParams.weights1;
        oldWeights0 = MathUtils.deepCopy(weights0);
        oldWeights1 = MathUtils.deepCopy(weights1);

        long oldWordCount = 0;
        long wordCount = 0;
        try {
            double mean_batch = 1.0/500000.0;
            int max_batch = 2000000;
            double batch_size = Math.min(Math.log(1 - rand.nextDouble()) / -mean_batch, max_batch);
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                boolean hasNextSentence = inputStream.readNextSentence(vocab);
                if (!hasNextSentence) break;
                int[] sentence = inputStream.getCurrentSentence();
                // if end of file, finish
                if (sentence.length == 0) {
                    continue;
                }

                wordCount = inputStream.getWordCount();
                
                if (wordCount - oldWordCount >= batch_size) {
                    batch_size = Math.min(Math.log(1 - rand.nextDouble()) / -mean_batch, max_batch);
                    if (rand.nextFloat() <= 0.999) {
                        RawSemanticSpace space = new RawSemanticSpace(vocab, weights0, false);
                        System.out.println(men.evaluateSpacePearson(space));
                    }
                    System.out.println("vector: " + weights0[2][0] + " " + weights0[2][1]);
                    MathUtils.minusInPlace(weights0, oldWeights0);
                    MathUtils.minusInPlace(weights1, oldWeights1);
                    deltaParams = new SkipGramParameters(alpha, wordCount - oldWordCount, weights0, weights1);
                    modelParams = (SkipGramParameters) parameterMessager
                            .sendUpdate(deltaParams).getContent();
                    weights0 = modelParams.weights0;
                    weights1 = modelParams.weights1;
                    oldWeights0 = MathUtils.deepCopy(weights0);
                    oldWeights1 = MathUtils.deepCopy(weights1);
                    alpha = modelParams.alpha;
                    oldWordCount = wordCount;
                    System.out.println("alpha: " + alpha);
                    System.out.println("wordCount: " + wordCount);
                    if (rand.nextFloat() <= 0.999) {
                        RawSemanticSpace space = new RawSemanticSpace(vocab, weights0, false);
                        System.out.println(men.evaluateSpacePearson(space));
                    }
                }
                trainSentence(sentence);
            }
            System.out.println("end of file: " + wordCount);
            // TODO: if wordCount do something
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
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
                    } else if (negativeSamples > 0) {
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
                                        * weights1[target][j];
                            }
                            double a2 = sigmoidTable.getSigmoid(z2);
                            gradient = (double) ((label - a2) * alpha);
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * weights1[target][j];
                            }
                            for (int j = 0; j < projectionLayerSize; j++) {
                                weights1[target][j] += gradient
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

}

