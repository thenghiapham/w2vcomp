package word2vec.multitask;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import space.SemanticSpace;
import vocab.VocabEntry;
import word2vec.MultiThreadWord2Vec;
import common.MathUtils;
import common.SimpleMatrixUtils;
import common.exception.ValueException;
import common.wordnet.WordNetAdj;
import demo.TestConstants;

public class AntonymWord2Vec extends MultiThreadWord2Vec{
    public static final int DEFAULT_SYNONYM_SAMPLES = 5;
    public static final double DEFAULT_MARGIN = 0.1;
    protected int synonymSamples = DEFAULT_SYNONYM_SAMPLES;
    protected double margin = DEFAULT_MARGIN;
    protected WordNetAdj wordnetAdj;
    
    public AntonymWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int synonymSamples, WordNetAdj wordNetAdj, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
        this.wordnetAdj = wordNetAdj;
        this.synonymSamples = synonymSamples;
    }
    
    public AntonymWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int synonymSamples, WordNetAdj wordNetAdj, double subSample,  String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample, menFile);
        this.wordnetAdj = wordNetAdj;
        this.synonymSamples = synonymSamples;
    }

    public void trainSentence(int[] sentence) {
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        // TODO: set the thing here
        double r = 1.0;

        
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
            String percept =    targetWord.word;      

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
                                weights1[iParentIndex][j] += gradient * r
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
            boolean isWNAdj = wordnetAdj.hasAdjSynset(percept);
            
            if (isWNAdj) {
                String[][] antoSynoSimNyms = wordnetAdj.getRandomSynoAntoSimNyms(percept);
                if (antoSynoSimNyms[0].length == 0) {
                    continue;
                }
                String antonym = antoSynoSimNyms[0][rand.nextInt(antoSynoSimNyms[0].length)];
                int antonymIndex = vocab.getWordIndex(antonym);
                if (antonymIndex == -1) continue;
                SimpleMatrix antonymError = new SimpleMatrix(projectionLayerSize, 1);
                double gradient=0;
                
                // TODO: counting
//                mmWordsPerRun++;
                
                //mapping word
                SimpleMatrix wordVector = new SimpleMatrix(projectionLayerSize, 1, true, weights0[wordIndex]));
                SimpleMatrix antonymVector = new SimpleMatrix(projectionLayerSize,1,true, weights0[antonymIndex]);
                SimpleMatrix err_cos_row = SimpleMatrixUtils.cosineDerivative(wordVector, antonymVector);
                
                double cos = SimpleMatrixUtils.cosine(wordVector, antonymVector);
                ArrayList<String> synonyms = WordNetAdj.getShuffledSynonyms(antoSynoSimNyms[1], antoSynoSimNyms[2]);
                int k=0;
                int l = 0;
                int index = 0;
                while (l < synonymSamples || index < synonyms.size()) {
                    int neg_sample;
                    while (true && index < synonyms.size()){
                        String synonym = synonyms.get(index);
                        neg_sample = vocab.getWordIndex(synonym);       //random sampling and then based on neighboorhood
                        if (neg_sample != -1)
                            break;
                        index++;
                    }
                    SimpleMatrix synonymVector = new SimpleMatrix(projectionLayerSize,1,true, weights0[neg_sample]);
                    
                    double cosSynonym = SimpleMatrixUtils.cosine(wordVector, synonymVector);
                    if (cosSynonym - cos>= margin) continue;
                    k++;
                    
                    //calculate error with respect to the cosine
                    antonymError = antonymError.plus(MathUtils.cosineDerivative(wordVector, synonymVector));
                    index++;
                    l++;
                }
                gradient = (double) (alpha* r);
                antonymError = antonymError.minus(err_cos_row.scale(k));
                a1error_temp  = a1error_temp.plus(antonymError.scale(gradient));
                
                
                
                // Learn weights input -> hidden
                for (int j = 0; j < projectionLayerSize; j++) {
                    weights0[wordIndex][j] += a1error_temp.get(j, 0);
                    a1error[j] = 0;
                }
            }
        }
        
    }
}
