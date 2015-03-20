package demo;

import java.io.IOException;
import java.util.HashMap;

import common.IOUtils;

import space.SemanticSpace;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;

public class SentenceLearning {
    public static void main(String[] args) throws IOException {
        String modelFile = "/home/angeliki/sas/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z_n5_m0.5_5_r11.0_r220.0l1.0E-4.hs";
        int vecSize = 300;
        boolean hs = true;
        int negativeSample = 0;
        if (args.length > 0) {
            modelFile = args[0];
            vecSize = Integer.parseInt(args[1]);
            hs = Boolean .parseBoolean(args[2]);
            negativeSample = Integer.parseInt(args[3]);
        }
        
        HashMap<String, String> sentences_id = IOUtils.readFastMappingSentences("/home/angeliki/sas/visLang/fast-mapping/create_jobs/CFsets_nonce-1.csv.ready.bold.withcos.untok");
        String[] sentences = new String[sentences_id.size()];
        String[] ids = new String[sentences_id.size()];
        int i=0;
        for (String id : sentences_id.keySet()){
            sentences[i] = sentences_id.get(id);
            ids[i++] = id;
        }
        
        String vocabFile = TestConstants.VOCABULARY_FILE;
        
        Paragraph2Vec p2v = new SkipgramPara2Vec(modelFile, vocabFile, vecSize, 5, hs, negativeSample, 0,1e-3, 100);
        
        
        p2v.trainParagraphVector(sentences);
        double[][] sentenceVector = p2v.getParagraphVectors();
        SemanticSpace space = new SemanticSpace(ids, sentenceVector);
        space.exportSpace("/home/angeliki/sas/visLang/fast-mapping/mapping_chimeras_3b_cnnsvd_normalized.txt");
        
        
    }
}
