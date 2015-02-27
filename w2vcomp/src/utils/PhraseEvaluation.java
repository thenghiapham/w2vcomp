package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import common.IOUtils;
import common.classifier.SvmCrossValidation;
import common.classifier.SvmTrainTest;
import common.correlation.CosineFeatureExtractor;
import common.correlation.ParsedPhraseCorrelation;
import common.correlation.PhraseCorrelation;
import common.correlation.SentenceClassification;
import composition.WeightedAdditive;
import space.RawSemanticSpace;

public class PhraseEvaluation {
    public static final String SVM_DIR = "/home/thenghia.pham/libsvm-3.20";
    public static String[][] getDatasetInfo() {
        String d = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/eval/";
        String[][] datasets = 
               {
//                {"sick", d + "SICK_train_trial_rel.txt", "sim-parse"},
//                {"sick-rte", d + "SICK_train_trial_rte.txt", "svm-cos"},
//                {"onwn", d + "STS.all.surprise.OnWN.txt", "sim"},
//                {"onwn2", d + "STS.all.OnWN.txt", "sim"},
//                {"msr-test", d + "STS.all.MSRvid.test.txt", "sim"},
//                {"msr-train", d + "STS.all.MSRvid.train.txt", "sim"},
                {"imdb", d + "imdb2.txt", "svm-vec"}
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
                CosineFeatureExtractor extracter = new CosineFeatureExtractor(path, path + ".feature");
                String[] labels = extracter.getLabels();
                double[][] features = extracter.getCosineFeatures(space, add);
                SvmCrossValidation crossVad = new SvmCrossValidation(SVM_DIR);
                System.out.println(name + ": " + crossVad.crossValidation(labels, features, 10, ""));
            } else if (type.equals("svm-vec")) {
                SentenceClassification extracter = new SentenceClassification(path);
                String[] labels = extracter.getLabels();
                double[][] features = extracter.getSentenceVectors(space, add);
                String divFile = path + ".div";
                ArrayList<String> lines = IOUtils.readFile(divFile);
                int[] div = new int[lines.size()];
                for (int i = 0; i < lines.size(); i++) {
                    div[i] = Integer.parseInt(lines.get(i));
                }
                int[] trainIndices = SvmTrainTest.getIndices(div, 1);
                int[] testIndices = SvmTrainTest.getIndices(div, 2);
                String[] trainLabels = SvmTrainTest.extractLabels(labels, trainIndices);
                String[] testLabels = SvmTrainTest.extractLabels(labels, testIndices);
                double[][] trainData = SvmTrainTest.extractData(features, trainIndices);
                double[][] testData = SvmTrainTest.extractData(features, testIndices);
                SvmTrainTest trainTest = new SvmTrainTest(SVM_DIR);
                System.out.println(name + ": " + trainTest.trainTest(trainLabels, trainData, testLabels, testData, ""));
            }
        }
    }
}
