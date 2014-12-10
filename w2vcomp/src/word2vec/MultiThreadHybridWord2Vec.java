package word2vec;

import vocab.VocabEntry;

public class MultiThreadHybridWord2Vec extends MultiThreadWord2Vec {
    public MultiThreadHybridWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample);
    }
    
    public MultiThreadHybridWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, menFile);
    }

    @Override
    public void trainSentence(int[] sentence) {
        trainSentenceCBow(sentence);
        trainSentenceSkipGram(sentence);
    }
    
    public void trainSentenceSkipGram(int[] sentence) {
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
    
    public void trainSentenceCBow(int[] sentence) {
        // train with the sentence
        for (int wordPosition = 0; wordPosition < sentence.length; wordPosition++) {
            // random a number to decrease the window size
             int leeway = rand.nextInt(windowSize);
//            int leeway = 0;
            trainWordAt(wordPosition, sentence, leeway);
        }
    }

    protected void trainWordAt(int wordPosition, int[] sentence, int leeway) {

        int wordIndex = sentence[wordPosition];

        // no way it will go here
        if (wordIndex == -1)
            return;

        int sentenceLength = sentence.length;
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int iWordIndex = 0;

        for (int i = 0; i < projectionLayerSize; i++) {
            a1[i] = 0;
            a1error[i] = 0;
        }

        // sum all the vectors in the window
        
        int wordCount = 0;
        for (int i = leeway; i < windowSize * 2 + 1 - leeway; i++) {

            if (i != windowSize) {
                int currentPos = wordPosition - windowSize + i;
                if (currentPos < 0 || currentPos >= sentenceLength)
                    continue;
                iWordIndex = sentence[currentPos];
                if (iWordIndex == -1) {
                    // System.out.println("shouldn't be here");
                    continue;
                }
                wordCount++;
                // stupid c here, use something else, say d,e
                for (int j = 0; j < projectionLayerSize; j++)
                    a1[j] += weights0[iWordIndex][j];
            }
        }

        if (wordCount == 0) {
//            return;
        } else {
            for (int j = 0; j < projectionLayerSize; j++) {
                a1[j] /= wordCount;
            }
        }
        
        if (hierarchicalSoftmax) {
            VocabEntry word = vocab.getEntry(wordIndex);
            for (int bit = 0; bit < word.code.length(); bit++) {

                double z2 = 0;
                int iParentIndex = word.ancestors[bit];
                // Propagate hidden -> output
                for (int i = 0; i < projectionLayerSize; i++) {
                    z2 += a1[i] * weights1[iParentIndex][i];
                }
                double a2 = sigmoidTable.getSigmoid(z2);
                if (a2 == 0 || a2 == 1)
                    continue;

                // 'g' is the gradient multiplied by the learning rate
                double gradient = (double) ((1 - (word.code.charAt(bit) - 48) - a2) * alpha);

                // Propagate errors output -> hidden
                for (int i = 0; i < projectionLayerSize; i++) {
                    a1error[i] += gradient * weights1[iParentIndex][i];
                }
                // Learn weights hidden -> output
                for (int i = 0; i < projectionLayerSize; i++) {
                    weights1[iParentIndex][i] += gradient * a1[i];
                }
            }
        }

        // NEGATIVE SAMPLING
        if (negativeSamples > 0) {
            int target;
            int label;

            // "generating" the positive sample + k negative samples
            // the objective function is true_positive + sigma_k(false_negative)
            for (int j = 0; j < negativeSamples + 1; j++) {
                if (j == 0) {
                    target = wordIndex;
                    label = 1;
                } else {
                    target = unigram.randomWordIndex();
                    if (target == 0)
                        target = rand.nextInt(vocab.getVocabSize() - 1) + 1;
                    if (target == wordIndex)
                        continue;
                    label = 0;
                }

                double z2 = 0;
                for (int i = 0; i < projectionLayerSize; i++) {
                    z2 += a1[i] * negativeWeights1[target][i];
                }
                double gradient;
                double a2 = sigmoidTable.getSigmoid(z2);
                gradient = (double) ((label - a2) * alpha);

                for (int i = 0; i < projectionLayerSize; i++) {
                    a1error[i] += gradient * negativeWeights1[target][i];
                }
                for (int i = 0; i < projectionLayerSize; i++) {
                    negativeWeights1[target][i] += gradient * a1[i];
                }
            }
        }

        // hidden -> in
        for (int i = leeway; i < windowSize * 2 + 1 - leeway; i++) {
            if (i != windowSize) {
                int iPos = wordPosition - windowSize + i;
                if (iPos < 0 || iPos >= sentenceLength)
                    continue;
                iWordIndex = sentence[iPos];
                if (iWordIndex == -1)
                    continue;
                for (int j = 0; j < projectionLayerSize; j++) {
                    weights0[iWordIndex][j] += a1error[j];
                }
            }
        }
    }
}
