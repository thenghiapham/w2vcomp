package common;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashSet;

import tree.Tree;

public class ParsedPhraseVectorPrinter {
    String[][] parsedPhrasePairs;
    String[][] surfacePhrasePairs;
    String[] parsedPhrases;
    String[] surfacePhrases;
    public ParsedPhraseVectorPrinter(String dataset) {
        readDataset(dataset);
    }
    
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        convertRawData(data);
    }
    
    protected void convertRawData(ArrayList<String> data) {
        double[] golds = new double[data.size()];
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
    
    
    protected void printVector(BufferedWriter writer, double[] vectors, int featureIndex) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < vectors.length; i++) {
            buffer.append(i + featureIndex);
            buffer.append(":");
            buffer.append(vectors[i]);
            if (i != vectors.length - 1) {
                buffer.append(" ");
            }
        }
    }
}
