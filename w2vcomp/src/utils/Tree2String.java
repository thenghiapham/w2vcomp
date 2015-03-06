package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import demo.W2vProperties;
import tree.Tree;
import io.sentence.BasicTreeInputStream;

public class Tree2String {
    public static void printArray2Stream(String[] sentence, BufferedWriter outWriter) throws IOException {
        int length = sentence.length;
        for (int i = 0; i < length - 1; i++) {
            outWriter.write(sentence[i]);
            outWriter.write(" ");
        }
        if (length > 0) {
            outWriter.write(sentence[length - 1]);
        }
        outWriter.write("\n");
    }
    
    public static void treeStream2StringStream(BasicTreeInputStream inStream, BufferedWriter outWriter) throws IOException {
        Tree tree;
        tree = inStream.readTree();
        while (tree != null) {
            String[] sentence = tree.getSurfaceWords();
            printArray2Stream(sentence, outWriter);
            tree = inStream.readTree();
        }
                
    }
    
    public static void treeFile2StringFile(String inFile, String outFile) throws IOException {
        BasicTreeInputStream inStream = new BasicTreeInputStream(inFile);
        BufferedWriter outWriter = new BufferedWriter(new FileWriter(outFile));
        treeStream2StringStream(inStream, outWriter);
        inStream.close();
        outWriter.close();
    }
    
    public static void treeDir2StringDir(String srcDir, String destDir) throws IOException {
        File dir = new File(srcDir);
        File[] files = dir.listFiles();
        for (File file: files) {
            String inFileName = file.getName();
            String outFile = destDir + "/" + inFileName;
            treeFile2StringFile(file.getAbsolutePath(), outFile);
        }
    }
    
    public static void treeDir2StringFile(String inDir, String outFile) throws IOException {
        File dir = new File(inDir);
        File[] files = dir.listFiles();
        BufferedWriter outWriter = new BufferedWriter(new FileWriter(outFile));

        for (File file: files) {
            BasicTreeInputStream inStream = new BasicTreeInputStream(file.getAbsolutePath());
            treeStream2StringStream(inStream, outWriter);
            inStream.close();
        }
        outWriter.close();
    }
    
    public static void main(String[] args) throws IOException {
        String configFile = args[0];
        String outputFile = args[1];
        W2vProperties properties = new W2vProperties(configFile);
        String inDir = properties.getProperty("STrainDir");
//        String outFile = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/corpus/rawWiki.txt";
        treeDir2StringFile(inDir, outputFile);
    }
}
