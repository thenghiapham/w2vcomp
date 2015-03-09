package word2vec.extra;

import common.MathUtils;
import common.exception.ValueException;

import space.SemanticSpace;
import vocab.VocabEntry;
import word2vec.SingleThreadWord2Vec;
import io.word.Phrase;

public class MMSkipgramCosine extends SingleThreadWord2Vec {
    double alphaRatio = 0;
    public MMSkipgramCosine(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double alphaRatio, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, 0, subSample);
        this.alphaRatio = alphaRatio; 
    }
    
    public MMSkipgramCosine(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double alphaRatio , double subSample,  String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, 0, subSample, menFile);
        this.alphaRatio = alphaRatio;
    }

    public void trainSentence(int[] sentence) {
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        
        
        boolean updateAtTheEnd=false;
        
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

            VocabEntry targetWord = vocab.getEntry(wordIndex);
            //modality 1
            for (int i = start; i < windowSize * 2 + 1 - start; i++) {
                if (i != windowSize) {
                    int iPos = wordPosition - windowSize + i;
                    if (iPos < 0 || iPos >= sentenceLength)
                        continue;
                    iWordIndex = sentence[iPos];
                    if (iWordIndex == -1)
                        continue;

                    //for (int j = 0; j < projectionLayerSize; j++)
                     //   a1error[j] = 0;
                    VocabEntry context = vocab.getEntry(iWordIndex);
                    // HIERARCHICAL SOFTMAX
                    if (hierarchicalSoftmax) {
                        for (int bit = 0; bit < context.code.length(); bit++) {
                            double z2 = 0;
                            int iParentIndex = context.ancestors[bit];
                            // Propagate hidden -> output
                            for (int j = 0; j < projectionLayerSize; j++) {
                                z2 += weights0[wordIndex][j]
                                        * weights1[iParentIndex][j];
                            }

                            double a2 = sigmoidTable.getSigmoid(z2);
                            if (a2 == 0 || a2 == 1)
                                continue;
                            // 'g' is the gradient multiplied by the learning
                            // rate
                            double gradient = (double) ((1 - (context.code
                                    .charAt(bit) - 48) - a2) * alpha);
                            // Propagate errors output -> hidden
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * weights1[iParentIndex][j];
                            }
                            // Learn weights hidden -> output
                            for (int j = 0; j < projectionLayerSize; j++) {
                                weights1[iParentIndex][j] += gradient
                                        * weights0[wordIndex][j];
                            }
                        }
                    }

                }
                    
             }
            // Learn weights input -> hidden
            if (!updateAtTheEnd){
                for (int j = 0; j < projectionLayerSize; j++) {
                    weights0[wordIndex][j] += a1error[j];
                }
            }
        
                /*************    FOR SECOND MODALITY   ****************/
            if (!updateAtTheEnd){
                for (int i = 0; i < projectionLayerSize; i++) {
                    a1error[i] = 0;
                }
            }
        
        
            String percept =    targetWord.word;      
            int jPerceptIndex = images.getIndex(percept);
       
            // NEGATIVE SAMPLING  
            if (alphaRatio > 0 && jPerceptIndex!=-1) {
            
                double gradient = alpha * alphaRatio;
                double[] cosineDerivate = MathUtils.cosineDerivative(weights0[wordIndex], 
                        negativeWeights1Images[jPerceptIndex]);
                for (int j = 0; j < projectionLayerSize; j++) {
                    a1error[j] += gradient
                        * cosineDerivate[j];
                }
                
            }
            // Learn weights input -> hidden
            for (int j = 0; j < projectionLayerSize; j++) {
                weights0[wordIndex][j] += a1error[j];
            }
        
        }

    }

    public double[] getCors() throws ValueException{
        return images.pairwise_cor(new SemanticSpace(vocab, weights0,false));
    }

    @Override
    public void trainSinglePhrase(Phrase phrase, int[] pseudoSentence) {
        // TODO Auto-generated method stub

    }
    
    
}

