package common.correlation;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


import common.IOUtils;
import composition.BasicComposition;

import space.SemanticSpace;
import tree.Tree;

public class CosineFeaturePrinter{

    String name = "";
    String[][] parsePhrasePairs;
    String[][] surfacePhrasePairs;
    // the info of hrases that need to be composed
    // it contains a list of string tuples (word1, word2, phrases)
    String[] parsedPhrase;
    String[] surfacePhrase;
    double[][] features;
    String[] labels;

    public CosineFeaturePrinter(String dataset, String featureFile) {
        readDataset(dataset);
        readFeature(featureFile);
    }
    
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        convertRawData(data);
    }
    
    public void readFeature(String featureFile) {
        ArrayList<String> data = IOUtils.readFile(featureFile);
        features = new double[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            String[] elements = data.get(i).split("( |\t)");
            features[i] = new double[elements.length];
            for (int j = 0; j < elements.length; j++) {
                features[i][j] = Double.parseDouble(elements[j]);
            }
        }
    }
    
    protected void convertRawData(ArrayList<String> data) {
        labels = new String[data.size()];
        parsePhrasePairs = new String[data.size()][2];
        surfacePhrasePairs = new String[data.size()][2];
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
            labels[i] = elements[2];
        }
        
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
    
    public double[][] getCosineFeatures(SemanticSpace space, BasicComposition composition) {
        SemanticSpace phraseSpace = composition.composeSpace(space, surfacePhrase);
        return getCosineFeaturesPhraseSpace(phraseSpace);
    }
    
    public double[][] getCosineFeaturesPhraseSpace(SemanticSpace surfaceSpace) {
        int featureLength = features[0].length;
        double[][] result = new double[surfacePhrasePairs.length][featureLength + 1];
        
        for (int i = 0; i < surfacePhrasePairs.length; i++) {
            result[i][0] = surfaceSpace.getSim(surfacePhrasePairs[i][0], surfacePhrasePairs[i][1]);
            System.arraycopy(features[i], 0, result[i], 1, featureLength);
        }
        return result;
    }
    
    String[] getLabels() {
        return labels;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String[] getSurfacePhrase() {
        return surfacePhrase;
    }
    
    public static void main(String[] args) throws IOException {
    }
}
