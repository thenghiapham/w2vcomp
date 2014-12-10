package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import composition.BasicComposition;
import space.CompositionalSemanticSpace;
import space.SMSemanticSpace;
import space.SemanticSpace;
import tree.Tree;

public class ParsedPhraseCosinePrinter {
    String[][] parsedPhrasePairs;
    String[][] surfacePhrasePairs;
    String[] parsedPhrases;
    String[] surfacePhrases;
    double[] golds;
    String[] features;
    public ParsedPhraseCosinePrinter(String dataset, String featureFile) {
        readDataset(dataset);
        ArrayList<String> featureList = IOUtils.readFile(featureFile);
        features = new String[featureList.size()];
        features = featureList.toArray(features);
    }
    
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        convertRawData(data);
    }
    
    protected void convertRawData(ArrayList<String> data) {
        golds = new double[data.size()];
        parsedPhrasePairs = new String[data.size()][2];
        surfacePhrasePairs = new String[data.size()][2];
        HashSet<String> parsePhraseSet = new HashSet<>();
        HashSet<String> surfacePhraseSet = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("\t");
            String phrase1 = elements[0];
            String phrase2 = elements[1];
            parsedPhrasePairs[i][0] = phrase1;
            parsedPhrasePairs[i][1] = phrase2;
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
        
        parsedPhrases = new String[parsePhraseSet.size()];
        int index = 0;
        for (String phrase: parsePhraseSet) {
            parsedPhrases[index] = phrase;
            index++;
        }
        surfacePhrases = new String[surfacePhraseSet.size()];
        index = 0;
        for (String phrase: surfacePhraseSet) {
            surfacePhrases[index] = phrase;
            index++;
        }
    }
    
    public void printCosines(String fileName, SemanticSpace space, BasicComposition compModel) throws IOException{
        SemanticSpace phraseSpace = compModel.composeSpace(space, surfacePhrases);
        printPhraseCosines(fileName, phraseSpace, true);
    }
    
    public void printCosines(String fileName, CompositionalSemanticSpace space) throws IOException{
        SMSemanticSpace phraseSpace = new SMSemanticSpace(parsedPhrases, space.getComposedMatrix(parsedPhrases));
        printPhraseCosines(fileName, phraseSpace, false);
    }
    
    public void printPhraseCosines(String fileName, SemanticSpace phraseSpace, boolean surface) throws IOException{
        int vectorSize = phraseSpace.getVectorSize();
        String[][] phrasePairs = null;
        if (surface)
            phrasePairs = surfacePhrasePairs;
        else
            phrasePairs = parsedPhrasePairs;
        double[] cosines = new double[phrasePairs.length];
        for (int i = 0; i < phrasePairs.length; i++) {
            double[] vector1 = phraseSpace.getVector(phrasePairs[i][0]).getMatrix().data;
            double[] vector2 = phraseSpace.getVector(phrasePairs[i][1]).getMatrix().data;
            cosines[i] = MathUtils.cosine(vector1, vector2);
            
        }
        printCosines(fileName, vectorSize, cosines);
    }
    
    protected void printCosines(String fileName, int vectorSize, double[] cosines) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (int i = 0; i < golds.length; i++) {
            writer.write("" + golds[i] + " 1:" + cosines[i]);
            String feature = features[i];
            String[] elements = feature.split("( |\n)");
            for (int j = 0; j < elements.length; j++) {
                writer.write(" " + (j+2) + ":" + elements[j]);
            }
            writer.write("\n");
        }
        writer.close();
    }
    
    
    public String[] getSurfacePhrases() {
        return surfacePhrases;
    }
    
    public String[] getParsedPhrases() {
        return parsedPhrases;
    }
}

