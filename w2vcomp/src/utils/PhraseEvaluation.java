package utils;

import java.io.File;
import java.io.IOException;

import common.correlation.ParsedPhraseCorrelation;
import common.correlation.PhraseCorrelation;
import composition.WeightedAdditive;
import space.RawSemanticSpace;

public class PhraseEvaluation {
    public static String[][] getDatasetInfo() {
        String d = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/eval/";
        String[][] datasets = 
               {{"sick", d + "SICK_train_trail_rel.txt", "sim-parse"},
                {"sick-rte", d + "SICK_train_trail_rte.txt", "svm-cos"},
                {"onwn", d + "STS.all.surprise.OnWN.txt", "sim"},
                {"msr-test", d + "STS.all.MSRvid.test.txt", "sim"},
                {"msr-train", d + "STS.all.MSRvid.train.txt", "sim"},
                {"imdb", d + "rubenstein-goodeneough.txt", "svm-vec"}
                };
        return datasets;
    }
                        
    public static void main(String args[]) throws IOException{
        String spaceDir = args[0];
        File[] files = (new File(spaceDir)).listFiles();
        String[][] datasets = getDatasetInfo();
        for (File file: files) {
            if (!file.getName().endsWith("bin")) continue;
            RawSemanticSpace space = RawSemanticSpace.readSpace(file.getAbsolutePath());
            System.out.println(file.getName());
            process(space, datasets);
        }
    }

    public static void process(RawSemanticSpace space, String[][] datasets) throws IOException{
        WeightedAdditive add = new WeightedAdditive(1.0, 1.0);
        for (String[] datasetInfo: datasets) {
            String name = datasetInfo[0];
            String path = datasetInfo[1];
            String type = datasetInfo[2];
            
            if (type.equals("sim-parse")) {
                ParsedPhraseCorrelation ppc = new ParsedPhraseCorrelation(path);
                System.out.println(name + ": " + ppc.evaluateSpacePearson(space, add) 
                        + " " + ppc.evaluateSpaceSpearman(space, add));
            } else if (type.equals("sim")){
                PhraseCorrelation pc = new PhraseCorrelation(path);
                System.out.println(name + ": " + pc.evaluateSpacePearson(space, add) 
                        + " " + pc.evaluateSpaceSpearman(space, add));
            } else if (type.equals("svm-cos")) {
                
            } else if (type.equals("svm-vec")) {
                
            }
        }
    }
}
