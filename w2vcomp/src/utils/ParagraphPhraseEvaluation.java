package utils;

import java.io.File;
import java.io.IOException;

import common.correlation.ParsedPhraseCorrelation;
import common.correlation.PhraseCorrelation;
import space.RawSemanticSpace;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;

public class ParagraphPhraseEvaluation {
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
        String modelDir = args[0];
        File[] files = (new File(modelDir)).listFiles();
        String[][] datasets = getDatasetInfo();
        String vocabFile = modelDir + "/bwu_lower.voc";
        for (File file: files) {
            if (!file.getName().endsWith("mdl")) continue;
            Paragraph2Vec p2v = new SkipgramPara2Vec(file.getAbsolutePath(), vocabFile, 400, 5, false, 10, 1e-5, 100);
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
            
            } else if (type.equals("svm-vec")) {
                
            }
        }
    }
}