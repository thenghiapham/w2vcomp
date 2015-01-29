package utils;

import java.io.File;
import java.io.IOException;

import common.SingleParsedPhraseVectorPrinter;
import common.SingleParsedPhraseVectorPrinter;
import composition.WeightedAdditive;
import space.CompositionSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;
import demo.TestConstants;

public class SvmSentimentPrinter {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello");
        String datasetFile = TestConstants.S_IMDB_FILE;
        String goldFile = TestConstants.S_IMDB_LABEL_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_IMDB_SVM_DIR;
        SingleParsedPhraseVectorPrinter printer = new SingleParsedPhraseVectorPrinter(datasetFile, goldFile);
        
        String compType = args[0];
        
        String vectorFile = args[1];
        String compFile = args[1];
        String modelFile = args[1];
        if ("w".equals(compType)) {
            printWeighted(printer, compFile, outDir, datasetName);
        } else if ("d".equals(compType)) {
            printDiagonal(printer, compFile, outDir, datasetName);
        } else if ("f".equals(compType)) {
            printFull(printer, compFile, outDir, datasetName);
        } else if ("a".equals(compType)) {
            System.out.println("Hello");
            printAdd(printer, vectorFile, outDir, datasetName);
        } else if ("p".equals(compType)) {
            String vocabFile = args[2];
            int vectorSize = Integer.parseInt(args[3]);
            boolean hs = Boolean.parseBoolean(args[4]);
            int neg = Integer.parseInt(args[5]);
            double subSample = Double.parseDouble(args[6]);
            printParagraph(printer, modelFile, vocabFile, vectorSize, hs, neg, subSample, outDir, datasetName);
        }
    }
    
    public static void printAdd(SingleParsedPhraseVectorPrinter printer, String vectorFile, String outDir, String suffix) throws IOException {
        RawSemanticSpace space = RawSemanticSpace.readSpace(vectorFile);
        String outFilePath = outDir + "add" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printVectors(outFilePath, space, new WeightedAdditive());
    }
    
    public static void printParagraph(SingleParsedPhraseVectorPrinter printer, String modelFile, String vocabFile, 
            int vectorSize, boolean hs, int negSample, double subSample, String outDir, String suffix) throws IOException {
        Paragraph2Vec p2v = new SkipgramPara2Vec(modelFile, vocabFile, vectorSize, 10, hs, negSample, subSample, 20);
        String[] sentences = printer.getSurfacePhrases();
        p2v.trainParagraphVector(sentences);
        double[][] sentenceVector = p2v.getParagraphVectors();
        RawSemanticSpace space = new RawSemanticSpace(sentences, sentenceVector);
        String outFilePath = outDir + "para" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printVectors(outFilePath, space);
    }
    
    public static void printWeighted(SingleParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "weighted" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printVectors(outFilePath, compSpace);
    }
    
    public static void printDiagonal(SingleParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "diagonal" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printVectors(outFilePath, compSpace);
    }
    
    public static void printFull(SingleParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "full" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printVectors(outFilePath, compSpace);
    }
}
