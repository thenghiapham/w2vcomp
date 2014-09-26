package common.correlation;

import java.util.ArrayList;
import java.util.HashSet;


import common.DataStructureUtils;
import common.IOUtils;
import composition.BasicComposition;

import space.CompositionSemanticSpace;
import space.SMSemanticSpace;
import space.SemanticSpace;
import tree.Tree;

/**
 * This class provides utility methods to evaluate composition models.
 * It uses a composition model to compose the vector representations of phrases
 * and computes the correlation between cosine similarities of phrases and 
 * the similarities/relatedness given by gold standard (human)
 * @author thenghiapham
 *
 */
public class ParsedPhraseCorrelation{

    MenCorrelation parseCorrelation;
    MenCorrelation surfaceCorrelation;
    
    // the info of hrases that need to be composed
    // it contains a list of string tuples (word1, word2, phrases)
    String[] parsedPhrase;
    String[] surfacePhrase;

    public ParsedPhraseCorrelation(String dataset) {
        readDataset(dataset);
    }
    
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        double[] golds = new double[data.size()];
        String[][] parsePhrasePairs = new String[data.size()][2];
        String[][] surfacePhrasePairs = new String[data.size()][2];
        HashSet<String> parsePhraseSet = new HashSet<>();
        HashSet<String> surfacePhraseSet = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("\t");
            String phrase1 = elements[0];
            String phrase2 = elements[1];
            parsePhrasePairs[i][0] = phrase1;
            parsePhrasePairs[i][1] = phrase2;
            String surfacePhrase1 = Tree.fromPennTree(phrase1).getSurfaceString();
            String surfacePhrase2 = Tree.fromPennTree(phrase2).getSurfaceString();
            surfacePhrasePairs[i][0] = surfacePhrase1;
            surfacePhrasePairs[i][0] = surfacePhrase2;
            parsePhraseSet.add(phrase1);
            parsePhraseSet.add(phrase2);
            surfacePhraseSet.add(surfacePhrase1);
            surfacePhraseSet.add(surfacePhrase1);
            golds[i] = Double.parseDouble(elements[2]);
        }
        parseCorrelation = new MenCorrelation(parsePhrasePairs, golds);
        surfaceCorrelation = new MenCorrelation(surfacePhrasePairs, golds);
        
        // TODO: change string sets to arrays
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
    
    
    /**
     * Evaluate the composition model using Pearson correlation
     * @param space
     * @param composition
     * @return
     */
    public double evaluateSpacePearson(SemanticSpace space, BasicComposition composition) {
        SemanticSpace phraseSpace = composition.composeSpace(space, surfacePhrase);
        return surfaceCorrelation.evaluateSpacePearson(phraseSpace);
    }
    
    
    public double evaluateSpacePearson(CompositionSemanticSpace space) {
        SMSemanticSpace phraseSpace = new SMSemanticSpace(parsedPhrase, space.getComposedMatrix(parsedPhrase));
        return parseCorrelation.evaluateSpacePearson(phraseSpace);
   }
    
    /**
     * Evaluate the composition model using Spearman correlation
     * @param space
     * @param composition
     * @return
     */
    
    public double evaluateSpaceSpearman(SMSemanticSpace space, BasicComposition composition) {
        SemanticSpace phraseSpace = composition.composeSpace(space, surfacePhrase);
        return surfaceCorrelation.evaluateSpaceSpearman(phraseSpace);
    }
    
    public double evaluateSpaceSpearman(CompositionSemanticSpace space) {
        SMSemanticSpace phraseSpace = new SMSemanticSpace(parsedPhrase, space.getComposedMatrix(parsedPhrase));
        return parseCorrelation.evaluateSpaceSpearman(phraseSpace);
    }
}
