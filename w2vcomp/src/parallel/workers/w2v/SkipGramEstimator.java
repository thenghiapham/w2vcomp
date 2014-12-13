package parallel.workers.w2v;

import io.sentence.SentenceInputStream;
import io.sentence.SubSamplingSentenceInputStream;

import java.io.IOException;
import java.util.Random;

import parallel.comm.ParameterMessager;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterEstimator;
import vocab.Vocab;
import vocab.VocabEntry;
import word2vec.UniGram;

import common.SigmoidTable;

import demo.TestConstants;

public class SkipGramEstimator implements ParameterEstimator{
    Vocab vocab;
    //double alpha;
    
    boolean          hierarchicalSoftmax;
    int              negativeSamples;

    double            subSample;

    int              projectionLayerSize;
    int              windowSize;
    
    protected UniGram          unigram;
    protected SigmoidTable     sigmoidTable;

    SkipGramParameters modelParams;
    
    SentenceInputStream inputStream;
    Random rand = new Random();
    
    
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
    }
    
    
    
    @Override
    public void run(Integer worker_id, ModelParameters init, ParameterMessager parameterMessager) {
        vocab.loadVocab(TestConstants.S_VOCABULARY_FILE);
        if (negativeSamples > 0) {
            this.unigram = new UniGram(vocab);
        }
        vocab.assignCode();
        System.out.println("Dictionary size :" + vocab.getVocabSize());
        System.out.println("2nd word :" + vocab.getEntry(2).word);
        
        modelParams = (SkipGramParameters) init;

        ParameterUpdatePoller parameterUpdatePoller = new ParameterUpdatePoller(worker_id, modelParams, parameterMessager);
        long wordCount = 0;
        try {
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
                
                parameterUpdatePoller.checkUpdate(wordCount, vocab);
                trainSentence(sentence);
            }
            System.out.println("end of file: " + wordCount);
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
                                z2 += modelParams.getWeights0()[iWordIndex][j]
                                        * modelParams.getWeights1()[iParentIndex][j];
                            }

                            double a2 = sigmoidTable.getSigmoid(z2);
                            if (a2 == 0 || a2 == 1)
                                continue;
                            // 'g' is the gradient multiplied by the learning
                            // rate
                            double gradient = (double) ((1 - (word.code
                                    .charAt(bit) - 48) - a2) * modelParams.getAlpha());
                            // Propagate errors output -> hidden
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * modelParams.getWeights1()[iParentIndex][j];
                            }
                            // Learn weights hidden -> output
                            modelParams.updatesCount1[iParentIndex]++;
                            for (int j = 0; j < projectionLayerSize; j++) {
                                modelParams.getWeights1()[iParentIndex][j] += gradient
                                        * modelParams.getWeights0()[iWordIndex][j];
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
                                z2 += modelParams.getWeights0()[iWordIndex][j]
                                        * modelParams.getWeights1()[target][j];
                            }
                            double a2 = sigmoidTable.getSigmoid(z2);
                            gradient = (double) ((label - a2) * modelParams.getAlpha());
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * modelParams.getWeights1()[target][j];
                            }
                            modelParams.updatesCount1[target]++;
                            for (int j = 0; j < projectionLayerSize; j++) {
                                modelParams.getWeights1()[target][j] += gradient
                                        * modelParams.getWeights0()[iWordIndex][j];
                            }
                        }
                    }
                    modelParams.updatesCount0[iWordIndex]++;
                    // Learn weights input -> hidden
                    for (int j = 0; j < projectionLayerSize; j++) {
                        modelParams.getWeights0()[iWordIndex][j] += a1error[j];
                    }
                }

            }

        }

    }

}

