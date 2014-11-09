package utils;

import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class WikCorpusSplit {
    public static void splitCorpus(String inputFile, int tokenNum, String prefix) throws IOException{
        int fileNum = 0;
        int tokenCount = 0;
        WordInputStream inputStream = new PushBackWordStream(new BufferedInputStream(new FileInputStream(inputFile)), 100);
        BufferedWriter writer = null;
        String outputFile = (prefix + "_" + (fileNum / 10)) + (fileNum % 10);
        String word = inputStream.readWord();
        while (!"".equals(word)) {
            if (tokenCount % tokenNum == 0) {
                writer = new BufferedWriter(new FileWriter(outputFile));
            }
            
            writer.write(word);
            
            word = inputStream.readWord();
            tokenCount++;
            
            if (tokenCount % tokenNum == 0) {
                writer.close();
                fileNum++;
                System.out.println(fileNum);
                outputFile = (prefix + "_" + (fileNum / 10)) + (fileNum % 10);
            } else {
                writer.write(" ");
            }
        }
    }
    
    public static void main(String[] args) throws IOException{
        String inputFile = args[0];
        int tokenNum = Integer.parseInt(args[1]);
        String prefix = args[2];
        splitCorpus(inputFile, tokenNum, prefix);
    }
}
