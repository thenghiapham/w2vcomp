package word2vec;

import vocab.VocabEntry;

public class MultiThreadContextSkipgram extends MultiThreadContextWord2Vec {

    public MultiThreadContextSkipgram(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample
            ) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample);
    }
    
    public MultiThreadContextSkipgram(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample,
            String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, menFile);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void trainSentence(int[] sentence, int[] history, int[] future) {
        // TODO Auto-generated method stub
        int[] historyPresentFuture = new int[history.length + sentence.length + future.length];
        System.arraycopy(history, 0, historyPresentFuture, 0, history.length);
        System.arraycopy(sentence, 0, historyPresentFuture, history.length, sentence.length);
        System.arraycopy(future, 0, historyPresentFuture, history.length + sentence.length, future.length);
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentence.length;
        int contextLength = historyPresentFuture.length;
        int iWordIndex = 0;
        for (int wordPosition = 0; wordPosition < sentenceLength; wordPosition++) {

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
                    int iPos = history.length + (wordPosition - windowSize + i);
                    if (iPos < 0 || iPos >= contextLength)
                        continue;
                    iWordIndex = historyPresentFuture[iPos];
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
}
