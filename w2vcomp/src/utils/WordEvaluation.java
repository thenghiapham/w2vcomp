package utils;

import java.io.File;
import java.io.IOException;

import common.correlation.FeatureNorm;
import common.correlation.MenCorrelation;
import common.correlation.Toefl;
import common.correlation.WordAnalogyEvaluation;
import space.RawSemanticSpace;

public class WordEvaluation {
    public static String[][] getDatasetInfo() {
        String d = "/mnt/povobackup/clic/georgiana.dinu/IP/eval/";
        String[][] datasets = {{"men", d + "MEN_dataset_lemma_form_full", "sim"},
                {"ws-rel", d + "cleaned-wordsim_relatedness_goldstandard.txt", "sim"},
                {"ws-sim", d + "cleaned-wordsim_similarity_goldstandard.txt", "sim"},
                {"ws-tot", d + "cleaned-wordsim_simrel_goldstandard.txt", "sim"},
                {"rg", d + "rubenstein-goodeneough.txt", "sim"},
                {"tfl", d + "toefl-test-set.txt", "tfl"},
                {"mcrae", d + "mcrae-dataset.txt", "selpref"},
                {"up", d + "up-dataset.txt", "selpref"},
                {"analogy", d + "questions-words.txt", "anal"},
                {"aamp", d + "aamp-gold-standard.txt", "clst"},
                {"battig", d + "battig-gold-standard.txt", "clst"},
                {"esslli", d + "esslli-gold-standard.txt", "clst"}
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
        for (String[] datasetInfo: datasets) {
            String name = datasetInfo[0];
            String path = datasetInfo[1];
            String type = datasetInfo[2];
            
            if (type.equals("sim")) {
                MenCorrelation men = new MenCorrelation(path);
                System.out.println(name + ": " + men.evaluateSpacePearson(space) 
                        + " " + men.evaluateSpaceSpearman(space));
            } else if (type.equals("tfl")){
                Toefl toefl = new Toefl(path);
                System.out.println(name + ": " + toefl.evaluation(space));
            } else if (type.equals("selpref")) {
                FeatureNorm fnorm = new FeatureNorm(path);
                double[] correlation = fnorm.evaluate(space);
                System.out.println(name + ": " + correlation[0] + " " + correlation[1]);
            } else if (type.equals("anal")) {
                WordAnalogyEvaluation eval = new WordAnalogyEvaluation(path);
                double[] accs = eval.evaluation(space);
                System.out.println(name + ": " + accs[0] + " " + accs[1] + " " +accs[2]);
            }
        }
    }
}
