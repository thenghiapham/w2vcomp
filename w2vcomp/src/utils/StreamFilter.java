package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import vocab.Vocab;

public class StreamFilter {
    protected Vocab vocab;
    public StreamFilter(String vocabFile, int frequency) {
        vocab = new Vocab(frequency);
        vocab.loadVocab(vocabFile);
    }
    public void filter(String inputFile, String outputFile) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        String line = reader.readLine();
        while (line != null) {
            line = filter(line);
            if (line.length() != 0)
                writer.write(line);
            line = reader.readLine();
        }
        reader.close();
        writer.close();
    }
    
    public String filter(String inputLine) {
        if ("".equals(inputLine)) return inputLine;
        StringBuffer sbResult = new StringBuffer();
        String[] words = inputLine.split(" ");
        for (String word: words) {
            if (vocab.getWordIndex(word) != -1) {
                sbResult.append(word + " ");
            }
        }
        if (sbResult.length() == 0) return "";
        sbResult.setCharAt(sbResult.length() - 1, '\n');
        return sbResult.toString();
    }
    
    public static void main(String[] args) throws IOException{
        String inputFile = args[0];
        String vocabFile = args[1];
        String outputFile = args[2];
        int frequency = 0;
        if (args.length == 4) {
            frequency = Integer.parseInt(args[3]);
        }
        StreamFilter filter = new StreamFilter(vocabFile, frequency);
        filter.filter(inputFile, outputFile);
    }
}
