package word2vec.multitask;

import java.util.ArrayList;
import java.util.HashSet;

import org.ejml.simple.SimpleMatrix;

import vocab.VocabEntry;
import word2vec.MultiThreadWord2Vec;
import common.SimpleMatrixUtils;
import common.wordnet.WordNetAdj;
import common.wordnet.WordNetNoun;

public class AntonymWord2Vec extends MultiThreadWord2Vec{
    public static final int DEFAULT_SYNONYM_SAMPLES = 5;
    public static final double DEFAULT_MARGIN = 0.4;
    public static final double DEFAULT_ANTONYM_IMPORTANCE = 8.0;
    protected int synonymSamples = DEFAULT_SYNONYM_SAMPLES;
    protected double margin = DEFAULT_MARGIN;
    
    protected WordNetAdj wordnetAdj;
    protected WordNetNoun wordnetNoun;
    protected HashSet<String> forbiddenWords;
    
    public AntonymWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int synonymSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
        this.synonymSamples = synonymSamples;
    }
    
    public AntonymWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int synonymSamples, double subSample,  String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample, menFile);
        this.synonymSamples = synonymSamples;
    }

    public void setForbiddenWords(HashSet<String> forbiddenWords) {
        this.forbiddenWords = forbiddenWords;
    }
    public void setWordNetAdj(WordNetAdj wordNetAdj) {
        this.wordnetAdj = wordNetAdj;
    }
    public void setWordNetNoun(WordNetNoun wordNetNoun) {
        this.wordnetNoun = wordNetNoun;
    }
    
    public void trainSentence(int[] sentence) {
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        // TODO: set the thing here
        double r = DEFAULT_ANTONYM_IMPORTANCE;

        
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
            if (wordnetNoun != null) {
                SimpleMatrix a1error_temp = new SimpleMatrix(1, a1error.length);
                boolean isWNNoun = wordnetNoun.hasNounSynset(percept);
                
                if (isWNNoun && !forbiddenWords.contains(percept)) {
                    String[][] antoSynoSimNyms = wordnetNoun.getRandomSynoAntoSimNyms(percept, forbiddenWords);
                    if (antoSynoSimNyms[0].length == 0) {
                        continue;
                    }
                    String antonym = antoSynoSimNyms[0][rand.nextInt(antoSynoSimNyms[0].length)];
                    int antonymIndex = vocab.getWordIndex(antonym);
                    if (antonymIndex == -1) continue;
                    SimpleMatrix antonymError = new SimpleMatrix(1, projectionLayerSize);
                    double gradient=0;
                    
                    // TODO: counting
    //                mmWordsPerRun++;
                    
                    //mapping word
                    SimpleMatrix wordVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[wordIndex]);
                    SimpleMatrix antonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[antonymIndex]);
                    SimpleMatrix err_cos_row = SimpleMatrixUtils.cosineDerivative(wordVector, antonymVector);
                    
                    double cos = SimpleMatrixUtils.cosine(wordVector, antonymVector);
                    ArrayList<String> synonyms = WordNetNoun.getShuffledSynonyms(antoSynoSimNyms[1], antoSynoSimNyms[2]);
                    int k=0;
                    int l = 0;
                    int index = 0;
                    while (l < synonymSamples && index < synonyms.size()) {
                        int synonymIndex = -1;
                        while (index < synonyms.size()){
    //                        System.out.println("here " + index);
                            String synonym = synonyms.get(index);
                            synonymIndex = vocab.getWordIndex(synonym);       //random sampling and then based on neighboorhood
                            if (synonymIndex != -1)
                                break;
                            index++;
                            
                        }
                        if (synonymIndex == -1) break;
                        SimpleMatrix synonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[synonymIndex]);
                        
                        double cosSynonym = SimpleMatrixUtils.cosine(wordVector, synonymVector);
                        index++;
                        l++;
                        if (cosSynonym - cos>= margin) continue;
                        k++;
                        
                        //calculate error with respect to the cosine
                        antonymError = antonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, synonymVector));
                        
                    }
                    gradient = (double) (alpha* r);
                    antonymError = antonymError.minus(err_cos_row.scale(k));
                    a1error_temp  = a1error_temp.plus(antonymError.scale(gradient));
                    
                    double[] errorArray = a1error_temp.getMatrix().data;
                    
                    // Learn weights input -> hidden
                    
                    for (int j = 0; j < projectionLayerSize; j++) {
                        weights0[wordIndex][j] += errorArray[j];
                        a1error[j] = 0;
                    }
                }
            }
            
            if (wordnetAdj != null) {
                SimpleMatrix a1error_temp = new SimpleMatrix(1, a1error.length);
                boolean isWNAdj = wordnetAdj.hasAdjSynset(percept);
                
                if (isWNAdj && !forbiddenWords.contains(percept)) {
                    String[][] antoSynoSimNyms = wordnetAdj.getRandomSynoAntoSimNyms(percept, forbiddenWords);
                    if (antoSynoSimNyms[0].length == 0) {
                        continue;
                    }
                    String antonym = antoSynoSimNyms[0][rand.nextInt(antoSynoSimNyms[0].length)];
                    int antonymIndex = vocab.getWordIndex(antonym);
                    if (antonymIndex == -1) continue;
                    SimpleMatrix antonymError = new SimpleMatrix(1, projectionLayerSize);
                    double gradient=0;
                    
                    // TODO: counting
    //                mmWordsPerRun++;
                    
                    //mapping word
                    SimpleMatrix wordVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[wordIndex]);
                    SimpleMatrix antonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[antonymIndex]);
                    SimpleMatrix err_cos_row = SimpleMatrixUtils.cosineDerivative(wordVector, antonymVector);
                    
                    double cos = SimpleMatrixUtils.cosine(wordVector, antonymVector);
                    ArrayList<String> synonyms = WordNetAdj.getShuffledSynonyms(antoSynoSimNyms[1], antoSynoSimNyms[2]);
                    int k=0;
                    int l = 0;
                    int index = 0;
                    while (l < synonymSamples && index < synonyms.size()) {
                        int synonymIndex = -1;
                        while (index < synonyms.size()){
    //                        System.out.println("here " + index);
                            String synonym = synonyms.get(index);
                            synonymIndex = vocab.getWordIndex(synonym);       //random sampling and then based on neighboorhood
                            if (synonymIndex != -1)
                                break;
                            index++;
                            
                        }
                        if (synonymIndex == -1) break;
                        SimpleMatrix synonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[synonymIndex]);
                        
                        double cosSynonym = SimpleMatrixUtils.cosine(wordVector, synonymVector);
                        index++;
                        l++;
                        if (cosSynonym - cos>= margin) continue;
                        k++;
                        
                        //calculate error with respect to the cosine
                        antonymError = antonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, synonymVector));
                        
                    }
                    gradient = (double) (alpha* r);
                    antonymError = antonymError.minus(err_cos_row.scale(k));
                    a1error_temp  = a1error_temp.plus(antonymError.scale(gradient));
                    
                    double[] errorArray = a1error_temp.getMatrix().data;
                    
                    // Learn weights input -> hidden
                    
                    for (int j = 0; j < projectionLayerSize; j++) {
                        weights0[wordIndex][j] += errorArray[j];
                        a1error[j] = 0;
                    }
                }
            }
        }
        
    }
}
