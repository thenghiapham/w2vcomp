package common.correlation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


import common.IOUtils;
import composition.BasicComposition;
import composition.WeightedAdditive;
import demo.TestConstants;

import space.CompositionSemanticSpace;
import space.CompositionalSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.SMSemanticSpace;
import space.SemanticSpace;
import space.WeightedCompositionSemanticSpace;
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
    String name = "";
    
    // the info of hrases that need to be composed
    // it contains a list of string tuples (word1, word2, phrases)
    String[] parsedPhrase;
    String[] surfacePhrase;

    protected ParsedPhraseCorrelation() {
        
    }
    
    public ParsedPhraseCorrelation(String dataset) {
        readDataset(dataset);
    }
    
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        convertRawData(data);
        // TODO: change string sets to arrays
    }
    
    protected void convertRawData(ArrayList<String> data) {
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
            surfacePhrasePairs[i][1] = surfacePhrase2;
            parsePhraseSet.add(phrase1);
            parsePhraseSet.add(phrase2);
            surfacePhraseSet.add(surfacePhrase1);
            surfacePhraseSet.add(surfacePhrase2);
            golds[i] = Double.parseDouble(elements[2]);
        }
        parseCorrelation = new MenCorrelation(parsePhrasePairs, golds);
        surfaceCorrelation = new MenCorrelation(surfacePhrasePairs, golds);
        
        parsedPhrase = new String[parsePhraseSet.size()];
        int index = 0;
        for (String phrase: parsePhraseSet) {
            parsedPhrase[index] = phrase;
            index++;
        }
        surfacePhrase = new String[surfacePhraseSet.size()];
        index = 0;
        for (String phrase: surfacePhraseSet) {
            surfacePhrase[index] = phrase;
            index++;
        }
        
        
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
    
    
    public double evaluateSpacePearson(CompositionalSemanticSpace space) {
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
    
    public double evaluateSpaceSpearman(CompositionalSemanticSpace space) {
        SMSemanticSpace phraseSpace = new SMSemanticSpace(parsedPhrase, space.getComposedMatrix(parsedPhrase));
        return parseCorrelation.evaluateSpaceSpearman(phraseSpace);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public static void main(String[] args) throws IOException {
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/dbnc.cmp", true);
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(TestConstants.S_COMPOSITION_FILE, true);
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/wbnc.cmp", true);
//        DiagonalCompositionSemanticSpace addSpace = DiagonalCompositionSemanticSpace.loadProjectionSpace("/home/thenghiapham/work/project/mikolov/output/wbnc.cmp", true);
//        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/bnc.cmp", true);
//        DiagonalCompositionSemanticSpace addSpace = DiagonalCompositionSemanticSpace.loadProjectionSpace("/home/thenghiapham/work/project/mikolov/output/dbnc40.cmp", true);
        RawSemanticSpace space = RawSemanticSpace.readSpace(TestConstants.S_VECTOR_FILE);
        WeightedAdditive add = new WeightedAdditive();
        ParsedPhraseCorrelation sickCorrelation = new ParsedPhraseCorrelation(TestConstants.S_SICK_FILE);
        System.out.println("sick add: " + sickCorrelation.evaluateSpacePearson(space, add));
        System.out.println("sick comp: " + sickCorrelation.evaluateSpacePearson(compSpace));
//        System.out.println("an add2: " + sickCorrelation.evaluateSpacePearson(addSpace));
    }
    
    
}
