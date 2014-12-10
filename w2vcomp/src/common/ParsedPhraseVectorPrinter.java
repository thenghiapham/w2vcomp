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

public class ParsedPhraseVectorPrinter {
    String[][] parsedPhrasePairs;
    String[][] surfacePhrasePairs;
    String[] parsedPhrases;
    String[] surfacePhrases;
    double[] golds;
    String[] features;
    public ParsedPhraseVectorPrinter(String dataset) {
        readDataset(dataset);
        features = null;
    }
    
    public ParsedPhraseVectorPrinter(String dataset, String featureFile) {
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
    
    public void printVectors(String fileName, SemanticSpace space, BasicComposition compModel) throws IOException{
        SemanticSpace phraseSpace = compModel.composeSpace(space, surfacePhrases);
        printPhraseVectors(fileName, phraseSpace, true);
    }
    
    public void printVectors(String fileName, CompositionalSemanticSpace space) throws IOException{
        SMSemanticSpace phraseSpace = new SMSemanticSpace(parsedPhrases, space.getComposedMatrix(parsedPhrases));
        printPhraseVectors(fileName, phraseSpace, false);
    }
    
    public void printPhraseVectors(String fileName, SemanticSpace phraseSpace, boolean surface) throws IOException{
        int vectorSize = phraseSpace.getVectorSize();
        String[][] phrasePairs = null;
        if (surface)
            phrasePairs = surfacePhrasePairs;
        else
            phrasePairs = parsedPhrasePairs;
        double[][][] vectors = new double[phrasePairs.length][2][vectorSize];
        for (int i = 0; i < phrasePairs.length; i++) {
            vectors[i][0] = SimpleMatrixUtils.normalize(phraseSpace.getVector(phrasePairs[i][0]),1).getMatrix().data;
            vectors[i][1] = SimpleMatrixUtils.normalize(phraseSpace.getVector(phrasePairs[i][1]),1).getMatrix().data;
        }
        printVectors(fileName, vectorSize, vectors, features);
    }
    
    protected void printVectors(String fileName, int vectorSize, double[][][] vectorPairs, String[] features) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (int i = 0; i < golds.length; i++) {
            writer.write("" + golds[i] + " ");
            printVector(writer, vectorPairs[i][0], 1);
            writer.write(" ");
            printVector(writer, vectorPairs[i][1], 1 + vectorSize);
            if (features != null) {
                String feature = features[i];
                String[] elements = feature.split("( |\n)");
                for (int j = 0; j < elements.length; j++) {
                    writer.write(" " + (j+1 + vectorSize + vectorSize) + ":" + elements[j]);
                }
            }
            writer.write("\n");
        }
        writer.close();
    }
    
    protected void printVector(BufferedWriter writer, double[] vector, int featureIndex) throws IOException {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < vector.length; i++) {
            buffer.append(i + featureIndex);
            buffer.append(":");
            buffer.append(vector[i]);
            if (i != vector.length - 1) {
                buffer.append(" ");
            }
        }
        writer.write(buffer.toString());
    }
    
    public String[] getSurfacePhrases() {
        return surfacePhrases;
    }
    
    public String[] getParsedPhrases() {
        return parsedPhrases;
    }
}

