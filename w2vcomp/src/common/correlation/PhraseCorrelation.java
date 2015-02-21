package common.correlation;

import java.util.ArrayList;
import java.util.HashSet;

import space.SemanticSpace;
import common.IOUtils;
import composition.BasicComposition;

public class PhraseCorrelation {
    MenCorrelation surfaceCorrelation;
    String[] uniquePhrases;
    double[] golds;
    public PhraseCorrelation(String dataset) {
        readDataset(dataset);
    }
    
    private void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        convertRawData(data);
    }
    
    protected void convertRawData(ArrayList<String> data) {
        double[] golds = new double[data.size()];
        String[][] surfacePhrasePairs = new String[data.size()][2];
        HashSet<String> surfacePhraseSet = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("\t");
            String phrase1 = elements[0];
            String phrase2 = elements[1];
            surfacePhrasePairs[i][0] = phrase1;
            surfacePhrasePairs[i][1] = phrase2;
            surfacePhraseSet.add(phrase1);
            surfacePhraseSet.add(phrase2);
            golds[i] = Double.parseDouble(elements[2]);
        }
        surfaceCorrelation = new MenCorrelation(surfacePhrasePairs, golds);
        
        uniquePhrases = new String[surfacePhraseSet.size()];
        int index = 0;
        for (String phrase: surfacePhraseSet) {
            uniquePhrases[index] = phrase;
            index++;
        }
    }
    
    public double evaluateSpaceSpearman(SemanticSpace space, BasicComposition composition) {
        SemanticSpace phraseSpace = composition.composeSpace(space, uniquePhrases);
        return surfaceCorrelation.evaluateSpaceSpearman(phraseSpace);
    }
    
    public double evaluateSpacePearson(SemanticSpace space, BasicComposition composition) {
        SemanticSpace phraseSpace = composition.composeSpace(space, uniquePhrases);
        return surfaceCorrelation.evaluateSpacePearson(phraseSpace);
    }

    public double evaluatePhraseSpaceSpearman(SemanticSpace phraseSpace) {
        return surfaceCorrelation.evaluateSpaceSpearman(phraseSpace);
    }
    
    public double evaluatePhraseSpacePearson(SemanticSpace phraseSpace) {
        return surfaceCorrelation.evaluateSpacePearson(phraseSpace);
    }
    
    public String[] getPhrases() {
        return uniquePhrases;
    }
}
