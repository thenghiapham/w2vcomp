package utils;

import java.io.File;
import java.io.IOException;

import common.ParsedPhraseCosinePrinter;
import space.WeightedCompositionSemanticSpace;
import demo.TestConstants;

public class SentenceWordWeightPrinter {
    public static void main(String[] args) throws IOException {
        String datasetFile = TestConstants.S_RTE_FILE;
        String rteFeatureFile = TestConstants.S_RTE_FEATURE_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_OUT_DIR;
        ParsedPhraseCosinePrinter printer = new ParsedPhraseCosinePrinter(datasetFile, rteFeatureFile);
        String compFile = args[0];
        printWeighted(printer, compFile, outDir, datasetName);
    }
    
    public static void printWeighted(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "weighted_weigth" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printWordWeights(outFilePath, compSpace);
        outFilePath = outDir + "weighted_weigth_length" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printWordWeightLengths(outFilePath, compSpace);
    }
    
}