package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileMerger {
    public static final void mergeLineByLine(String inputFile1, 
            String inputFile2, String outputFile) throws IOException{
        BufferedReader reader1 = new BufferedReader(new FileReader(inputFile1));
        BufferedReader reader2 = new BufferedReader(new FileReader(inputFile2));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        String line1 = reader1.readLine();
        String line2 = reader2.readLine();
        while (line1 != null && line2 != null) {
            writer.write(line1 + "\t" + line2);
            line1 = reader1.readLine();
            line2 = reader2.readLine();
        }
        reader1.close();
        reader2.close();
        writer.close();
    }
    
    public static final void main(String args[]) throws IOException{
        mergeLineByLine(args[0], args[1], args[2]);
    }
}
