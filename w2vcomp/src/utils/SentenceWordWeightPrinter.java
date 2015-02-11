package utils;

import java.io.File;
import java.io.IOException;

import common.ParsedPhraseCosinePrinter;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;
import demo.TestConstants;

public class SentenceWordWeightPrinter {
    public static void main(String[] args) throws IOException {
        String datasetFile = TestConstants.S_RTE_FILE;
        String rteFeatureFile = TestConstants.S_RTE_FEATURE_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_OUT_DIR;
        ParsedPhraseCosinePrinter printer = new ParsedPhraseCosinePrinter(datasetFile, rteFeatureFile);
        String compType = args[0];
        String compFile = args[1];
        String vectorFile = args[1];
        switch (compType) {
        case "w":
            printWeighted(printer, compFile, outDir, datasetName);
            break;
        case "add":
            printAdd(printer, vectorFile, outDir, datasetName);
            break;
        default:
            System.out.println("Composition type not recognized");
        }
            
            
        
    }
    
    public static void printWeighted(ParsedPhraseCosinePrinter printer, String compFile, String outDir, String suffix) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "weighted_weight" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printWordWeights(outFilePath, compSpace);
        outFilePath = outDir + "weighted_weight_length" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printWordWeightLengths(outFilePath, compSpace);
    }
    
    public static void printAdd(ParsedPhraseCosinePrinter printer, String vectorFile, String outDir, String suffix) throws IOException {
        RawSemanticSpace rawSpace = RawSemanticSpace.readSpace(vectorFile);
        String outFilePath = outDir + "add_length" + suffix;
        System.out.println("out file: " + outFilePath);
        printer.printWordWeightLengths(outFilePath, rawSpace);
    }
    
    
}