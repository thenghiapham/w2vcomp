package utils;

import java.io.File;
import java.io.IOException;

import common.ParsedPhraseCosinePrinter;
import composition.WeightedAdditive;
import space.CompositionSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;
import demo.TestConstants;

public class SvmCosinePrinter {
    public static void main(String[] args) throws IOException {
        String datasetFile = TestConstants.S_RTE_FILE;
        String rteFeatureFile = TestConstants.S_RTE_FEATURE_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_RTE_SVM_DIR;
        ParsedPhraseCosinePrinter printer = new ParsedPhraseCosinePrinter(datasetFile, rteFeatureFile);
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
    
    public static void printAdd(ParsedPhraseCosinePrinter printer, String vectorFile, String outDir, String suffix) throws IOException {
        RawSemanticSpace space = RawSemanticSpace.readSpace(vectorFile);
        String outFilePath = outDir + "add" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printCosines(outFilePath, space, new WeightedAdditive());
    }
    
    public static void printParagraph(ParsedPhraseCosinePrinter printer, String modelFile, String vocabFile, 
            int vectorSize, boolean hs, int negSample, double subSample, String outDir, String suffix) throws IOException {
        Paragraph2Vec p2v = new SkipgramPara2Vec(modelFile, vocabFile, vectorSize, 5, hs, negSample, subSample, 100);
        String[] sentences = printer.getSurfacePhrases();
        p2v.trainParagraphVector(sentences);
        double[][] sentenceVector = p2v.getParagraphVectors();
        RawSemanticSpace space = new RawSemanticSpace(sentences, sentenceVector);
        String outFilePath = outDir + "para" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printCosines(outFilePath, space, new WeightedAdditive());
    }
    
    public static void printWeighted(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "weighted" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printCosines(outFilePath, compSpace);
    }
    
    public static void printDiagonal(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "diagonal" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printCosines(outFilePath, compSpace);
    }
    
    public static void printFull(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "full" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printCosines(outFilePath, compSpace);
    }
}
