package word2vec;

import vocab.VocabEntry;

public class CBowPara2Vec extends Paragraph2Vec{

    public CBowPara2Vec(String networkFile, String vocabFile,
            int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample,
            int iterationNum) {
        super(networkFile, vocabFile, projectionLayerSize, windowSize,
                hierarchicalSoftmax, negativeSamples, subSample, iterationNum);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void trainSentence(int sentenceIndex, int[] wordIndexArray) {
        for (int wordPosition = 0; wordPosition < wordIndexArray.length; wordPosition++) {
            // random a number to decrease the window size
            int leeway = rand.nextInt(windowSize);
            trainWordAt(sentenceIndex, wordPosition, wordIndexArray, leeway);
        }
    }

    private void trainWordAt(int sentenceIndex, int wordPosition, int[] wordIndexArray, int leeway) {
        // TODO Auto-generated method stub
        int wordIndex = wordIndexArray[wordPosition];

        // no way it will go here
        if (wordIndex == -1)
            return;

        int sentenceLength = wordIndexArray.length;
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        
        int wordCount = 1;
        int iWordIndex = 0;

        for (int i = 0; i < projectionLayerSize; i++) {
            a1[i] = paragraphVectors[sentenceIndex][i];
            a1error[i] = 0;
        }

        // sum all the vectors in the window
        // the -leeway doesn't make much sense
        for (int i = leeway; i < windowSize ; i++) {
            if (i != windowSize) {
                wordCount++;
                int currentPos = wordPosition - windowSize + i;
                if (currentPos < 0 || currentPos >= sentenceLength)
                    continue;
                iWordIndex = wordIndexArray[currentPos];
                if (iWordIndex == -1) {
                    // System.out.println("shouldn't be here");
                    continue;
                }
                // stupid c here, use something else, say d,e
                for (int j = 0; j < projectionLayerSize; j++)
                    a1[j] += weights0[iWordIndex][j];
            }
        }
        
        for (int j = 0; j < projectionLayerSize; j++) {
            a1[j] /= wordCount;
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
            }
        }

        for (int j = 0; j < projectionLayerSize; j++) {
            paragraphVectors[sentenceIndex][j] += a1error[j] / wordCount;
        }
    }

}
