package common.wordnet;

import java.io.IOException;

public class AntonymSynonymPrinter {
    public static void main(String[] args) throws IOException {
        String adjFile = args[0];
        String antFile = args[1];
        WordNetAdj wordNetAdj = new WordNetAdj(adjFile);
        wordNetAdj.printAllInfo(antFile);
        
    }
}
