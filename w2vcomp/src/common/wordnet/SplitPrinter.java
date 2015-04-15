package common.wordnet;

import java.io.IOException;

public class SplitPrinter {
    public static void main(String[] args) throws IOException {
        String adjFile = args[0];
        String antFile = args[1];
        String wordFile = args[2];
        WordNetAdj wordNetAdj = new WordNetAdj(adjFile);
        wordNetAdj.printSplitInfo(antFile, wordFile);
        
    }
}
