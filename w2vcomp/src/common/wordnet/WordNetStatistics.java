package common.wordnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import common.IOUtils;

public class WordNetStatistics {
    public WordNetStatistics() {
        
    }
    public static ArrayList<String[]> getPair(String pairFile, String wordFile) {
        ArrayList<String[]> allPairs = IOUtils.readTupleList(pairFile);
        HashSet<String> removedWords = new HashSet<>(IOUtils.readFile(wordFile));
        ArrayList<String[]> result = new ArrayList<>();
        for (String[] pair: allPairs) {
            for (String word: pair) {
                if (removedWords.contains(word)) {
                    result.add(pair);
                    break;
                }
                    
            }
        }
        return result;
    }
    public static void main(String[] args) throws IOException{
        String allPairs = "/home/thenghiapham/ant_pairs.txt";
        String removedWords = "/home/thenghiapham/cross_validation.txt";
        String crossVadFile = "/home/thenghiapham/cross_pairs.txt";
        ArrayList<String[]> crossPairs = getPair(allPairs, removedWords);
        IOUtils.printListPair(crossPairs, crossVadFile);
    }
}

