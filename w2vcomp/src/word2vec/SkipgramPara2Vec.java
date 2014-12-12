package word2vec;

import vocab.VocabEntry;

public class SkipgramPara2Vec extends Paragraph2Vec{

    public SkipgramPara2Vec(String networkFile, String vocabFile,
            int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample,
            int iterationNum) {
        super(networkFile, vocabFile, projectionLayerSize, windowSize,
                hierarchicalSoftmax, negativeSamples, subSample, iterationNum);
    }

    @Override
    protected void trainSentence(int sentenceIndex, int[] wordIndexArray) {
        System.out.println("or here");
        double[] a1error = new double[projectionLayerSize];
        for (int wordPosition = 0; wordPosition < wordIndexArray.length; wordPosition++) {

            int wordIndex = wordIndexArray[wordPosition];

            // no way it will go here
            if (wordIndex == -1)
                continue;

            for (int i = 0; i < projectionLayerSize; i++) {
                a1error[i] = 0;
            }

            // random actual window size
            VocabEntry word = vocab.getEntry(wordIndex);

            for (int j = 0; j < projectionLayerSize; j++)
                a1error[j] = 0;

            // HIERARCHICAL SOFTMAX
            if (hierarchicalSoftmax) {
                for (int bit = 0; bit < word.code.length(); bit++) {
                    double z2 = 0;
                    int iParentIndex = word.ancestors[bit];
                    // Propagate hidden -> output
                    for (int j = 0; j < projectionLayerSize; j++) {
                        
                        z2 += paragraphVectors[sentenceIndex][j]
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
                }
            }

            // NEGATIVE SAMPLING
            if (negativeSamples > 0) {
                System.out.println("go here");
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
                        z2 += paragraphVectors[sentenceIndex][j]
                                * negativeWeights1[target][j];
                    }
                    double a2 = sigmoidTable.getSigmoid(z2);
                    gradient = (double) ((label - a2) * alpha);
                    for (int j = 0; j < projectionLayerSize; j++) {
                        a1error[j] += gradient
                                * negativeWeights1[target][j];
                    }
                }
            }
            // Learn weights input -> hidden
            for (int j = 0; j < projectionLayerSize; j++) {
                paragraphVectors[sentenceIndex][j] += a1error[j];
            }


        }
    }

}
