package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import composition.BasicComposition;
import space.CompositionalSemanticSpace;
import space.SMSemanticSpace;
import space.SemanticSpace;
import tree.Tree;

public class SingleParsedPhraseVectorPrinter {
    String[] parsedPhrases;
    String[] surfacePhrases;
    String[] golds;
    String[] features;
    
    public static String[] readStringArray(String file) {
        ArrayList<String> data = IOUtils.readFile(file);
        String[] arrayResult = new String[data.size()];
        arrayResult = data.toArray(arrayResult);
        return arrayResult;
    }
    
    public SingleParsedPhraseVectorPrinter(String dataset, String labelFile) {
        readDataset(dataset);
        features = null;
        golds = readStringArray(labelFile);
    }
    
    public SingleParsedPhraseVectorPrinter(String dataset, String featureFile, String labelFile) {
        readDataset(dataset);
        ArrayList<String> featureList = IOUtils.readFile(featureFile);
        features = new String[featureList.size()];
        features = featureList.toArray(features);
        golds = readStringArray(labelFile);
    }
    
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        convertRawData(data);
    }
    
    protected void convertRawData(ArrayList<String> data) {
        parsedPhrases = new String[data.size()];
        parsedPhrases = data.toArray(parsedPhrases);
        surfacePhrases = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            String treeString = data.get(i);
            String surfacePhrase = Tree.fromPennTree(treeString).getSurfaceString();
            surfacePhrases[i] = surfacePhrase;
        }
    }
    
    public void printVectors(String fileName, SemanticSpace space, BasicComposition compModel) throws IOException{
        SemanticSpace phraseSpace = compModel.composeSpace(space, surfacePhrases);
        printPhraseVectors(fileName, phraseSpace, true);
    }
    
    public void printVectors(String fileName, SemanticSpace phraseSpace) throws IOException{
        printPhraseVectors(fileName, phraseSpace, true);
    }
    
    public void printVectors(String fileName, CompositionalSemanticSpace space) throws IOException{
        SMSemanticSpace phraseSpace = new SMSemanticSpace(parsedPhrases, space.getComposedMatrix(parsedPhrases));
        printPhraseVectors(fileName, phraseSpace, false);
    }
    
    public void printPhraseVectors(String fileName, SemanticSpace phraseSpace, boolean surface) throws IOException{
        int vectorSize = phraseSpace.getVectorSize();
        String[] phrases = null;
        if (surface)
            phrases = surfacePhrases;
        else
            phrases = parsedPhrases;
        double[][] vectors = new double[phrases.length][vectorSize];
        for (int i = 0; i < phrases.length; i++) {
            vectors[i] = phraseSpace.getVector(phrases[i]).getMatrix().data;
//            vectors[i] = SimpleMatrixUtils.normalize(phraseSpace.getVector(phrases[i]),1).getMatrix().data;
        }
        printVectors(fileName, vectorSize, vectors, features);
    }
    
    protected void printVectors(String fileName, int vectorSize, double[][] vectorPairs, String[] features) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (int i = 0; i < golds.length; i++) {
            writer.write("" + golds[i] + " ");
            printVector(writer, vectorPairs[i], 1 + vectorSize);
            if (features != null) {
                String feature = features[i];
                String[] elements = feature.split("( |\n)");
                for (int j = 0; j < elements.length; j++) {
                    writer.write(" " + (j+1 + vectorSize) + ":" + elements[j]);
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
    
    public void printSurfaceString(String outputFile) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for (String phrase: surfacePhrases) {
            writer.write(phrase + "\n");
        }
        writer.close();
    }
    
    public static void main(String[] args) throws IOException{
        String parseFile = args[0];
        String labelFile = args[1];
        String phraseFile = args[2];
        SingleParsedPhraseVectorPrinter printer = new SingleParsedPhraseVectorPrinter(parseFile, labelFile);
        printer.printSurfaceString(phraseFile);
    }
}

