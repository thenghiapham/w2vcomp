package utils;

import java.io.File;
import java.io.IOException;

import common.ParsedPhraseVectorPrinter;
import composition.WeightedAdditive;

import space.CompositionSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;
import demo.TestConstants;

public class SvmVectorPrinter {
    public static void main(String[] args) throws IOException {
        String datasetFile = TestConstants.S_RTE_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_RTE_SVM_DIR;
        String rteFeatureFile = TestConstants.S_RTE_FEATURE_FILE;
        String compFile = TestConstants.S_COMPOSITION_FILE;
        
        String vectorFile = TestConstants.S_VECTOR_FILE;
        ParsedPhraseVectorPrinter printer = new ParsedPhraseVectorPrinter(datasetFile, rteFeatureFile);
        printAdd(printer, vectorFile, outDir, datasetName);
        printWeighted(printer, compFile, outDir, datasetName);
    }
    
    public static void printAdd(ParsedPhraseVectorPrinter printer, String vectorFile, String outDir, String suffix) throws IOException {
        RawSemanticSpace space = RawSemanticSpace.readSpace(vectorFile);
        String outFilePath = outDir + "add" + suffix;
        printer.printVectors(outFilePath, space, new WeightedAdditive());
    }
    
    public static void printWeighted(ParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "weighted" + suffix;
        printer.printVectors(outFilePath, compSpace);
    }
    
    public static void printDiagonal(ParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "diagonal" + suffix;
        printer.printVectors(outFilePath, compSpace);
    }
    
    public static void printFull(ParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "full" + suffix;
        printer.printVectors(outFilePath, compSpace);
    }
}
