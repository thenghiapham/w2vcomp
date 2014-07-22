package common;

import java.util.ArrayList;
import java.util.HashSet;

import composition.BasicComposition;

import space.SemanticSpace;

/**
 * This class provides utility methods to evaluate composition models.
 * It uses a composition model to compose the vector representations of phrases
 * and computes the correlation between cosine similarities of phrases and 
 * the similarities/relatedness given by gold standard (human)
 * @author thenghiapham
 *
 */
public class AdjNounCorrelation{

    MenCorrelation correlation;
    
    // the info of hrases that need to be composed
    // it contains a list of string tuples (word1, word2, phrases) 
    String[][] composeData;

    public AdjNounCorrelation(String dataset) {
        readDataset(dataset);
    }
    
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        double[] golds = new double[data.size()];
        String[][] phrasePairs = new String[data.size()][2];
        HashSet<String> phraseSet = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split(" ");
            String phrase1 = elements[0] + "_" + elements[1];
            String phrase2 = elements[2] + "_" + elements[3];
            phrasePairs[i][0] = phrase1;
            phrasePairs[i][1] = phrase2;
            phraseSet.add(phrase1);
            phraseSet.add(phrase2);
            golds[i] = Double.parseDouble(elements[4]);
        }
        correlation = new MenCorrelation(phrasePairs, golds);
        composeData = AdjNounCorrelation.convertComposeData(phraseSet);
    }
    
    /**
     * Turn a list of phrases into composing data
     * @param phraseSet
     * @return
     */
    public static String[][] convertComposeData(HashSet<String> phraseSet) {
        String[][] result = new String[phraseSet.size()][3];
        String[] phrases = new String[phraseSet.size()];
        phrases = phraseSet.toArray(phrases);
        for (int i = 0; i < phrases.length; i++) {
            String[] words = phrases[i].split("_");
            result[i][0] = words[0];
            result[i][1] = words[1];
            result[i][2] = phrases[i];
        }
        return result;
    }
    
    public String[][] getComposeData() {
        return composeData;
    } 
    
    public MenCorrelation getCorrelationObject() {
        return correlation;
    }
    
    /**
     * Evaluate the composition model using Pearson correlation
     * @param space
     * @param composition
     * @return
     */
    public double evaluateSpacePearson(SemanticSpace space, BasicComposition composition) {
        SemanticSpace phraseSpace = composition.composeSpace(space, composeData);
        return correlation.evaluateSpacePearson(phraseSpace);
    }
    
    /**
     * Evaluate the composition model using Spearman correlation
     * @param space
     * @param composition
     * @return
     */
    public double evaluateSpaceSpearman(SemanticSpace space, BasicComposition composition) {
        SemanticSpace phraseSpace = composition.composeSpace(space, composeData);
        return correlation.evaluateSpaceSpearman(phraseSpace);
    }
}
