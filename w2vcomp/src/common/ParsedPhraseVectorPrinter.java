package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import composition.BasicComposition;
import space.CompositionSemanticSpace;
import space.SMSemanticSpace;
import space.SemanticSpace;
import tree.Tree;

public class ParsedPhraseVectorPrinter {
    String[][] parsedPhrasePairs;
    String[][] surfacePhrasePairs;
    String[] parsedPhrases;
    String[] surfacePhrases;
    double[] golds;
    public ParsedPhraseVectorPrinter(String dataset) {
        readDataset(dataset);
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
    
    protected void printVectors(String fileName, SemanticSpace space, BasicComposition compModel) throws IOException{
        SemanticSpace phraseSpace = compModel.composeSpace(space, surfacePhrases);
        int vectorSize = phraseSpace.getVectorSize();
        double[][][] vectors = new double[surfacePhrasePairs.length][2][vectorSize];
        for (int i = 0; i < surfacePhrasePairs.length; i++) {
            vectors[i][0] = phraseSpace.getVector(surfacePhrasePairs[i][0]).getMatrix().data;
            vectors[i][1] = phraseSpace.getVector(surfacePhrasePairs[i][1]).getMatrix().data;
        }
        printVectors(fileName, vectorSize, vectors);
    }
    
    protected void printVectors(String fileName, CompositionSemanticSpace space) throws IOException{
        SMSemanticSpace phraseSpace = new SMSemanticSpace(parsedPhrases, space.getComposedMatrix(parsedPhrases));
        int vectorSize = phraseSpace.getVectorSize();
        double[][][] vectors = new double[parsedPhrasePairs.length][2][vectorSize];
        for (int i = 0; i < parsedPhrasePairs.length; i++) {
            vectors[i][0] = phraseSpace.getVector(parsedPhrasePairs[i][0]).getMatrix().data;
            vectors[i][1] = phraseSpace.getVector(parsedPhrasePairs[i][1]).getMatrix().data;
        }
        printVectors(fileName, vectorSize, vectors);
    }
    
    protected void printVectors(String fileName, int vectorSize, double[][][] vectorPairs) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (int i = 0; i < golds.length; i++) {
            writer.write("" + golds[i] + " ");
            printVector(writer, vectorPairs[i][0], 1);
            writer.write(" ");
            printVector(writer, vectorPairs[i][1], 1 + vectorSize);
            writer.write("\n");
        }
    }
    
    protected void printVector(BufferedWriter writer, double[] vector, int featureIndex) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < vector.length; i++) {
            buffer.append(i + featureIndex);
            buffer.append(":");
            buffer.append(vector[i]);
            if (i != vector.length - 1) {
                buffer.append(" ");
            }
        }
    }
}

