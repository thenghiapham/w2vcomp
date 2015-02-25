package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import space.SemanticSpace;
import common.IOUtils;

public class ClusterHelper {
    public ArrayList<String> wordList;
    public ClusterHelper(String dataset) {
        wordList = IOUtils.readFile(dataset);
    }
    
    public void printSims(SemanticSpace space, String outputFile) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for (String wordPos1: wordList) {
            String word1 = wordPos1.substring(0, wordPos1.length() - 2).toLowerCase();
            for (String wordPos2: wordList) {
                String word2 = wordPos2.substring(0, wordPos2.length() - 2).toLowerCase();
                writer.write(wordPos1 + " " + wordPos2 + " " + space.getSim(word1, word2));
                writer.write("\n");
            }
        }
        writer.close();
    }
}
