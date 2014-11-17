package word2vec;

import org.ejml.simple.SimpleMatrix;
import org.jblas.DoubleMatrix;

import common.IOUtils;
import common.MathUtils;
import common.SimpleMatrixUtils;
import common.exception.ValueException;
import demo.TestConstants;

import space.SemanticSpace;
import vocab.Vocab;
import vocab.VocabEntry;
import io.word.Phrase;

public class MmSkipNGramWithMappingMaxMargin extends SingleThreadWord2Vec {
    public MmSkipNGramWithMappingMaxMargin(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int negativeSamplesImages, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImages, subSample);
    }
    
    public MmSkipNGramWithMappingMaxMargin(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int negativeSamplesImages , double subSample,  String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImages, subSample, menFile);
    }

    public void trainSentence(int[] sentence) {
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        SimpleMatrix a2error;
        //DoubleMatrix a2error;

        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        
        boolean updateAtTheEnd=false;
        
        double r = 1.0;
        double lambda = TestConstants.lambda;
       
        
        
        
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
            //int start = 0;

            VocabEntry targetWord = vocab.getEntry(wordIndex);
            String percept =    targetWord.word;      
            int jPerceptIndex = images.getIndex(percept);
            if (jPerceptIndex == -1 || negativeSamplesImages!=-1)  r = 1.0; else  r= TestConstants.rate_multiplier_sft;
            
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
                      // a1error[j] = 0;
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
                                weights1[iParentIndex][j] += gradient *r
                                        * weights0[wordIndex][j];
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
                                    target = rand.nextInt(vocab.getVocabSize() - 1) + 1;
                                }
                                if (target == iWordIndex)
                                    continue;
                                label = 0;
                            }
                            double z2 = 0;
                            double gradient;
                            for (int j = 0; j < projectionLayerSize; j++) {
                                z2 += weights0[wordIndex][j]
                                        * negativeWeights1[target][j];
                            }
                            double a2 = sigmoidTable.getSigmoid(z2);
                            
                            gradient = (double) ((label - a2) * alpha);
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * negativeWeights1[target][j];
                            }
                            for (int j = 0; j < projectionLayerSize; j++) {
                                negativeWeights1[target][j] += gradient *r
                                        * weights0[wordIndex][j];
                            }
                        }
                    }
                   
                    
                    
                    // Learn weights input -> hidden
                    if (!updateAtTheEnd){
                        for (int j = 0; j < projectionLayerSize; j++) {
                            weights0[wordIndex][j] += a1error[j];
                            a1error[j] = 0;

                        }
                    }
                    
                }
                    
             }
            
           
        
            /*************    FOR SECOND MODALITY   ****************/
            
            SimpleMatrix a1error_temp = new SimpleMatrix(a1error.length, 1);
            if (jPerceptIndex == -1)  r = 1.0; else  r= TestConstants.rate_multiplier_grad;
            
            if (negativeSamplesImages != -1 && jPerceptIndex!=-1) {
                a2error = new SimpleMatrix(imageProjectionLayer.numRows(),imageProjectionLayer.numCols());
                SimpleMatrix der = new SimpleMatrix(TestConstants.imageDimensions, 1);
                double gradient=0;
                mmWordsPerRun++;
                
                //mapping word
                SimpleMatrix mapped_word_row = (new SimpleMatrix(1,projectionLayerSize,false, weights0[wordIndex])).mult(imageProjectionLayer);
                SimpleMatrix image = new SimpleMatrix(negativeWeights1Images[jPerceptIndex].length,1,true, negativeWeights1Images[jPerceptIndex]);
                SimpleMatrix err_cos_row = MathUtils.cosineDerivative(mapped_word_row, image);
                
                double cos = MathUtils.cosine(mapped_word_row, image);
                int k=0;
                for (int l = 0; l < negativeSamplesImages ; l++) {
                    int neg_sample;
                    while (true){
                        neg_sample = images.randomWordIndex();       //random sampling and then based on neighboorhood
                        if (neg_sample != jPerceptIndex)
                            break;
                    }
                    SimpleMatrix image_neg = new SimpleMatrix(negativeWeights1Images[neg_sample].length,1,true, negativeWeights1Images[neg_sample]);
                    
                    double cos_neg = MathUtils.cosine(mapped_word_row, image_neg);
                    if (cos-cos_neg >= TestConstants.margin) continue;
                    k+=1;
                    
                    //calculate error with respect to the cosine
                    der = der.minus(MathUtils.cosineDerivative(mapped_word_row, image_neg));
                }
                gradient = (double) (alpha* r);
                der = der.plus(err_cos_row.scale(k));
                a1error_temp  = a1error_temp.plus(imageProjectionLayer.mult(der).scale(gradient));
                //calculate error with respect to the projection layer
                a2error = a2error.plus(((new SimpleMatrix(weights0[wordIndex].length,1,true, weights0[wordIndex])).mult(der.transpose())).scale(gradient));
                a2error = a2error.minus(imageProjectionLayer.scale(lambda)); 
                //update projection layer
                imageProjectionLayer = imageProjectionLayer.plus(a2error);
                if (wordCount < 300000 ){
                    double norm = imageProjectionLayer.normF();
                    if (norm > TestConstants.threshold){
                        imageProjectionLayer = imageProjectionLayer.scale(TestConstants.threshold/norm);
                        //DOUBLEMATRIX: imageProjectionLayer.mmuli(threshold/norm);
                    }
                }
                
                // Learn weights input -> hidden
                for (int j = 0; j < projectionLayerSize; j++) {
                    weights0[wordIndex][j] += a1error_temp.get(j, 0);
                    a1error[j] = 0;
                }
            }
            
        
        }

    }

    public double[] getCors() throws ValueException{
        return images.pairwise_cor(new SemanticSpace(vocab, weights0, false));
    }

    @Override
    public void trainSinglePhrase(Phrase phrase, int[] pseudoSentence) {
        // TODO Auto-generated method stub

    }
    
    public void saveMappingFunction(String outFile, boolean tobinary){
        IOUtils.saveMatrix(outFile, SimpleMatrixUtils.to2DArray(imageProjectionLayer), tobinary);
    }
    
    
}

