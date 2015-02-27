package utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;


import common.correlation.FeatureNorm;
import common.correlation.MenCorrelation;
import common.correlation.Toefl;
import common.correlation.WordAnalogyEvaluation;
import space.NormalizedSemanticSpace;
import space.RawSemanticSpace;

public class WordEvaluation {
    public static int sizeSpace = 180000;
    public static String[][] getDatasetInfo() {
        String d = "/mnt/povobackup/clic/georgiana.dinu/IP/eval/";
        String nDir = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/eval/";
        
        String[][] datasets = {
                {"men", d + "MEN_dataset_lemma_form_full", "sim"},
                {"ws-rel", d + "cleaned-wordsim_relatedness_goldstandard.txt", "sim"},
                {"ws-sim", d + "cleaned-wordsim_similarity_goldstandard.txt", "sim"},
                {"ws-tot", d + "cleaned-wordsim_simrel_goldstandard.txt", "sim"},
                {"rg", d + "rubenstein-goodeneough.txt", "sim"},
                {"sim-lex", nDir + "simlex-999.txt", "sim"},
//                {"tfl", d + "toefl-test-set.txt", "tfl"},
//                {"mcrae", d + "mcrae-dataset.txt", "selpref"},
//                {"up", d + "up-dataset.txt", "selpref"},
//                {"aamp", d + "aamp-gold-standard.txt", "clst"},
//                {"battig", d + "battig-gold-standard.txt", "clst"},
//                {"esslli", d + "esslli-gold-standard.txt", "clst"},
//                {"analogy", nDir + "questions-words.txt", "anal"},
                };
        return datasets;
    }
                        
    public static void main(String args[]) throws IOException{
        String spaceDir = args[0];
        File[] files = (new File(spaceDir)).listFiles();
        String[][] datasets = getDatasetInfo();
        for (int i = 0; i < datasets.length; i++) {
            System.out.print("|l");
        }
        System.out.print("|l|\n");
        for (String[] datasetInfo: datasets) {
            String name = datasetInfo[0];
            System.out.print(" & " + name);
        }
        System.out.print(" & an & ansyn & ansem ");
        System.out.println("\\\\ \\hline");
        for (File file: files) {
            if (!file.getName().endsWith("bin")) continue;
            System.out.print(file.getName() + " & ");
            process(file, datasets);
            System.out.println("  &   &   & \\\\ \\hline");
        }
    }

    public static void process(File spaceFile, String[][] datasets) throws IOException{
        NumberFormat format = DecimalFormat.getInstance();
        format.setMaximumFractionDigits(0);
        String outDir = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/eval/";
        RawSemanticSpace space = RawSemanticSpace.readSpace(spaceFile.getAbsolutePath());
        for (String[] datasetInfo: datasets) {
            String name = datasetInfo[0];
            String path = datasetInfo[1];
            String type = datasetInfo[2];
            
            if (type.equals("sim")) {
                MenCorrelation men = new MenCorrelation(path);
//                System.out.println(name + ": " + men.evaluateSpacePearson(space) 
//                        + " " + men.evaluateSpaceSpearman(space));
                System.out.print(format.format(men.evaluateSpaceSpearman(space) * 100) + " & ");
            } else if (type.equals("tfl")){
                Toefl toefl = new Toefl(path);
//                System.out.println(name + ": " + toefl.evaluation(space));
                System.out.print(format.format(toefl.evaluation(space) * 100)+ " & ");
            } else if (type.equals("selpref")) {
                FeatureNorm fnorm = new FeatureNorm(path);
                double[] correlation = fnorm.evaluate(space);
//                System.out.println(name + ": " + correlation[0] + " " + correlation[1]);
                System.out.print(format.format(correlation[1] * 100)+ " & ");
            } else if (type.equals("anal")) {
                WordAnalogyEvaluation eval = new WordAnalogyEvaluation(path);
                String[] words = space.getWords();
                double[][] vectors = space.getVectors();
                NormalizedSemanticSpace normedSpace = null;
                if (sizeSpace >= words.length) {
                    normedSpace = new NormalizedSemanticSpace(words, vectors);
                } else {
                    String[] newWords = new String[sizeSpace];
                    System.arraycopy(words, 0, newWords, 0, sizeSpace);
                    double[][] newVectors = new double[sizeSpace][];
                    System.arraycopy(vectors, 0, newVectors, 0, sizeSpace);
                    normedSpace = new NormalizedSemanticSpace(newWords, newVectors);
                }
                double[] accs = eval.evaluation(normedSpace);
//                System.out.println(name + ": " + accs[0] + " " + accs[1] + " " +accs[2]);
                System.out.print(accs[1] + " & " +accs[2] + " & ");
            } else if (type.equals("clst")) {
                ClusterHelper helper = new ClusterHelper(path);
                helper.printSims(space, outDir + name + "/" + spaceFile.getName().replace(".bin", ".txt"));
                System.out.print(" & ");
            }
        }
    }
}
