package common.correlation;

import java.util.ArrayList;

import space.RawSemanticSpace;

import common.IOUtils;

public class ExportCluster {
    public static void exportSpace(String inputSpaceFile, String inputWordFile, String outputSpaceFile, String outputWordFile, String outputGroupFile) {
        RawSemanticSpace space = RawSemanticSpace.readSpace(inputSpaceFile);
        ArrayList<String> infos = IOUtils.readFile(inputWordFile);
        ArrayList<String> inputWords = new ArrayList<>();
        ArrayList<String> inputGroups = new ArrayList<>();
        for (String info: infos) {
            String[] elements = info.split("\\s");
            inputWords.add(elements[0]);
            inputGroups.add(elements[1]);
        }
        ArrayList<String> outputGroups = new ArrayList<>();
        RawSemanticSpace subSpace = space.getSubSpace(inputWords);
        subSpace.exportSentimentSpace(outputSpaceFile, outputWordFile, true);
        for (int i = 0; i < inputWords.size();i++) {
            if (subSpace.contains(inputWords.get(i))) {
                outputGroups.add(inputGroups.get(i));
            }
        }
        IOUtils.printToFile(outputGroupFile, outputGroups);
    }
    
    public static void main(String[] args) {
        exportSpace(args[0], args[1], args[2], args[3], args[4]);
    }
}
