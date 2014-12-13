package utils;

import java.io.File;
import java.io.IOException;

import common.ParsedPhraseCosinePrinter;
import composition.WeightedAdditive;

import space.CompositionSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;
import demo.TestConstants;

public class SvmCosinePrinter {
    public static void main(String[] args) throws IOException {
        String datasetFile = TestConstants.S_RTE_FILE;
        String rteFeatureFile = TestConstants.S_RTE_FEATURE_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_RTE_SVM_DIR;
        ParsedPhraseCosinePrinter printer = new ParsedPhraseCosinePrinter(datasetFile, rteFeatureFile);
        if (args.length == 1) {
            String vectorFile = args[0];
            printAdd(printer, vectorFile, outDir, datasetName);
                
        } else if (args.length == 2) {
            String compFile = args[0];
            String compType = args[1];
            if ("w".equals(compType)) {
                printWeighted(printer, compFile, outDir, datasetName);
            } else if ("d".equals(compType)) {
                printDiagonal(printer, compFile, outDir, datasetName);
            } else if ("f".equals(compType)) {
                printFull(printer, compFile, outDir, datasetName);
            }
        }
    }
    
    public static void printAdd(ParsedPhraseCosinePrinter printer, String vectorFile, String outDir, String suffix) throws IOException {
        RawSemanticSpace space = RawSemanticSpace.readSpace(vectorFile);
        String outFilePath = outDir + "add" + suffix;
        printer.printCosines(outFilePath, space, new WeightedAdditive());
    }
    
    public static void printWeighted(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "weighted" + suffix;
        printer.printCosines(outFilePath, compSpace);
    }
    
    public static void printDiagonal(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "diagonal" + suffix;
        printer.printCosines(outFilePath, compSpace);
    }
    
    public static void printFull(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "full" + suffix;
        printer.printCosines(outFilePath, compSpace);
    }
}
