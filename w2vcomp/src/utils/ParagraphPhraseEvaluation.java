package utils;

import java.io.File;
import java.io.IOException;

import common.classifier.SvmCrossValidation;
import common.correlation.CosineFeatureExtractor;
import common.correlation.ParsedPhraseCorrelation;
import common.correlation.PhraseCorrelation;
import common.correlation.SentenceClassification;
import space.RawSemanticSpace;
import word2vec.CBowPara2Vec;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;

public class ParagraphPhraseEvaluation {
    public static final String SVM_DIR = "/home/thenghia.pham/libsvm-3.20";
    public static String[][] getDatasetInfo() {
        String d = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/eval/";
        String[][] datasets = 
                {
                //{"sick", d + "SICK_train_trial_rel.txt", "sim-parse"},
                //{"sick-rte", d + "SICK_train_trial_rte.txt", "svm-cos"},
                //{"onwn", d + "STS.all.surprise.OnWN.txt", "sim"},
                {"onwn2", d + "STS.all.surprise.OnWN.txt", "sim"},
                {"msr-test", d + "STS.all.MSRvid.test.txt", "sim"},
                {"msr-train", d + "STS.all.MSRvid.train.txt", "sim"},
                //{"imdb", d + "imdb2.txt", "svm-vec"}
                };
        return datasets;
    }
                        
    public static void main(String args[]) throws IOException{
        String modelDir = args[0];
        File[] files = (new File(modelDir)).listFiles();
        String[][] datasets = getDatasetInfo();
        String vocabFile = modelDir + "/bwu_lower.voc";
         
        for (File file: files) {
            if (!file.getName().endsWith("mdl")) continue;
            int vectorSize = 400;
            if (file.getName().contains("300")) {
                vectorSize = 300;
            }
            Paragraph2Vec p2v = null;
            if (file.getName().contains("skip"))
                p2v = new SkipgramPara2Vec(file.getAbsolutePath(), vocabFile, vectorSize, 5, false, 10, 1e-5, 100);
            else
                p2v = new CBowPara2Vec(file.getAbsolutePath(), vocabFile, vectorSize, 5, false, 10, 1e-5, 100);
            System.out.println(file.getName());
            process(p2v, datasets);
        }
    }

    public static void process(Paragraph2Vec p2v, String[][] datasets) throws IOException{
        for (String[] datasetInfo: datasets) {
            String name = datasetInfo[0];
            String path = datasetInfo[1];
            String type = datasetInfo[2];
            
            if (type.equals("sim-parse")) {
                ParsedPhraseCorrelation ppc = new ParsedPhraseCorrelation(path);
                String[] sentences = ppc.getSurfacePhrase();
                p2v.trainParagraphVector(sentences);
                RawSemanticSpace space = new RawSemanticSpace(sentences, p2v.getParagraphVectors());
                System.out.println(name + ": " + ppc.evaluatePhraseSpacePearson(space) 
                        + " " + ppc.evaluatePhraseSpaceSpearman(space));
            } else if (type.equals("sim")){
                PhraseCorrelation pc = new PhraseCorrelation(path);
                String[] sentences = pc.getPhrases();
                p2v.trainParagraphVector(sentences);
                RawSemanticSpace space = new RawSemanticSpace(sentences, p2v.getParagraphVectors());
                System.out.println(name + ": " + pc.evaluatePhraseSpacePearson(space) 
                        + " " + pc.evaluatePhraseSpaceSpearman(space));
            } else if (type.equals("svm-cos")) {
                CosineFeatureExtractor extracter = new CosineFeatureExtractor(path, path + ".feature");
                String[] labels = extracter.getLabels();
                String[] sentences = extracter.getSurfacePhrase();
                p2v.trainParagraphVector(sentences);
                RawSemanticSpace space = new RawSemanticSpace(sentences, p2v.getParagraphVectors());
                double[][] features = extracter.getCosineFeaturesPhraseSpace(space);
                SvmCrossValidation crossVad = new SvmCrossValidation(SVM_DIR);
                System.out.println(name + ": " + crossVad.crossValidation(labels, features, 10, ""));
                
            } else if (type.equals("svm-vec")) {
                SentenceClassification extracter = new SentenceClassification(path);
                String[] labels = extracter.getLabels();
                String[] sentences = extracter.getSentences();
                p2v.trainParagraphVector(sentences);
                RawSemanticSpace space = new RawSemanticSpace(sentences, p2v.getParagraphVectors());
                double[][] features = extracter.getSentenceVectors(space);
                SvmCrossValidation crossVad = new SvmCrossValidation(SVM_DIR);
                System.out.println(name + ": " + crossVad.crossValidation(labels, features, 4, "-d 1"));
                
            }
        }
    }
}
