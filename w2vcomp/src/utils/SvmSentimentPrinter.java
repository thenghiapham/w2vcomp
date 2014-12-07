package utils;

import java.io.File;
import java.io.IOException;

import common.SingleParsedPhraseVectorPrinter;
import composition.WeightedAdditive;
import space.CompositionSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;
import demo.TestConstants;

public class SvmSentimentPrinter {
    public static void main(String[] args) throws IOException {
        String datasetFile = TestConstants.S_IMDB_FILE;
        String goldFile = TestConstants.S_IMDB_LABEL_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_IMDB_SVM_DIR;
        String compFile = TestConstants.S_COMPOSITION_FILE;
        
        String vectorFile = TestConstants.S_VECTOR_FILE;
        SingleParsedPhraseVectorPrinter printer = new SingleParsedPhraseVectorPrinter(datasetFile, goldFile);
        printAdd(printer, vectorFile, outDir, datasetName);
        printWeighted(printer, compFile, outDir, datasetName);
    }
    
    public static void printAdd(SingleParsedPhraseVectorPrinter printer, String vectorFile, String outDir, String suffix) throws IOException {
        RawSemanticSpace space = RawSemanticSpace.readSpace(vectorFile);
        String outFilePath = outDir + "add" + suffix;
        printer.printVectors(outFilePath, space, new WeightedAdditive());
    }
    
    public static void printWeighted(SingleParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "weighted" + suffix;
        printer.printVectors(outFilePath, compSpace);
    }
    
    public static void printDiagonal(SingleParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "diagonal" + suffix;
        printer.printVectors(outFilePath, compSpace);
    }
    
    public static void printFull(SingleParsedPhraseVectorPrinter printer, String compFile, String outDir, String suffix) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace(compFile, true);
        String outFilePath = outDir + "full" + suffix;
        printer.printVectors(outFilePath, compSpace);
    }
}
