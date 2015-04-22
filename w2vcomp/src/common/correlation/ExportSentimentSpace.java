package common.correlation;

import java.util.ArrayList;

import common.IOUtils;
import space.RawSemanticSpace;

public class ExportSentimentSpace {
    public static void exportSpace(String inputSpaceFile, String inputWordFile, String outputSpaceFile, String outputWordFile) {
        RawSemanticSpace space = RawSemanticSpace.readSpace(inputSpaceFile);
        ArrayList<String> inputWords = IOUtils.readFile(inputWordFile);
        RawSemanticSpace subSpace = space.getSubCapSpace(inputWords);
        subSpace.exportSentimentSpace(outputSpaceFile, outputWordFile, false);
        
    }
    
    public static void main(String[] args) {
        exportSpace(args[0], args[1], args[2], args[3]);
    }
}
