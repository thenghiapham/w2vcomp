package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.ejml.simple.SimpleMatrix;

import space.RawSemanticSpace;

public class PrintLength {
    public static HashSet<String> readDatasetWordset(String rawDatasetDir)throws IOException{
        HashSet<String> words = new HashSet<>();
        File rawDirFile = new File(rawDatasetDir);
        File[] datasets = rawDirFile.listFiles();
        for (File dataset:datasets) {
            BufferedReader reader = new BufferedReader(new FileReader(dataset));
            String line = reader.readLine();
            while (line != null) {
                String[] elements = line.split(" ");
                for (String word: elements) {
                    if (word.length() >= 1) {
                        words.add(word);
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        }
        return words;
    }
    
    public static void printLength(String spaceFile, String rawDatasetDir, String outputFile) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        HashSet<String> words = readDatasetWordset(rawDatasetDir);
        RawSemanticSpace space = RawSemanticSpace.readSpace(spaceFile);
        ArrayList<WordLength> wordLengths = new ArrayList<>();
        for (String word: words) {
            SimpleMatrix matrix = space.getVector(word);
            if (matrix != null) {
                WordLength wordLength = new WordLength(word, matrix.normF());
                wordLengths.add(wordLength);
            }
        }
        Collections.sort(wordLengths);
        for (WordLength wordLength: wordLengths) {
            writer.write(wordLength.toString() + "\n");
        }
        writer.close();
    }
   
    public static void main(String[] args) throws IOException {
        String dataDir = args[0];
        String spaceFile = args[1];
        String outFile = args[2];
        printLength(spaceFile, dataDir, outFile);
    }
    
}

class WordLength implements Comparable<WordLength> {
    static final DecimalFormat format = new DecimalFormat("#.000");
    String word;
    double length;
    
    public WordLength(String word, double length) {
        // TODO Auto-generated constructor stub
        this.word = word;
        this.length = length;
    }
    public int compareTo(WordLength arg0) {
        // TODO Auto-generated method stub
        if (length > arg0.length) {
            return 1;
        } else if (length < arg0.length) {
            return -1;
        } else {
            return word.compareTo(arg0.word);
        }
    }
    
    public String toString() {
        return word + ": " + format.format(length);
    }
}