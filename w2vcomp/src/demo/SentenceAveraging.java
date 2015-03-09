package demo;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

import space.SemanticSpace;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;

public class SentenceAveraging {
    public static void main(String[] args) throws IOException {
        String wordvecs = "/home/angeliki/sas/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z_n5_m0.5_5_r11.0_r220.0l1.0E-4.bin";
        int vecSize = 300;
        boolean hs = true;
        int negativeSample = 0;
        if (args.length > 0) {
            wordvecs = args[0];
            vecSize = Integer.parseInt(args[1]);
            hs = Boolean .parseBoolean(args[2]);
            negativeSample = Integer.parseInt(args[3]);
        }


	//import v2w mapping	
	SimpleMatrix v2w = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream("/home/angeliki/sas/visLang/fast-mapping/crossmodal_general_training/training_data/cnn_svd/mapping/funtionFile/v2w.txt")), false));
	//import visual space for foils
	SemanticSpace foilsSpace = SemanticSpace.importSpace("/home/angeliki/sas/visLang/fast-mapping/feature_extraction/cnn/foils/PER_SS.foils.cnn.cnn.features_svd300.dm");
	foilsSpace = new SemanticSpace(foilsSpace.getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(foilsSpace.getVectors())).mult(v2w)));
	//change name to differentiate from linguistic foils
	String[] foils = foilsSpace.getWords();
	double[][] vecs_foils = foilsSpace.getVectors();

	for (int i=0; i<foils.length;i++){
		foils[i] = foils[i]+"_image";
	}	
        //read linguistic space
        SemanticSpace wordsSpace = SemanticSpace.readSpace(wordvecs);
	String[] words = wordsSpace.getWords();
        double[][] vecs_words = wordsSpace.getVectors();
	//combine
	String[] allWords = new String[foils.length+words.length];
	double[][] vecs = new double[foils.length+words.length][300];
	for (int i=0; i<foils.length;i++){
                allWords[i] = foils[i];
		vecs[i] = vecs_foils[i];
        }
	for (int i=0; i<words.length;i++){
                allWords[i+foils.length] = words[i];
                vecs[i+foils.length] = vecs_words[i];
        }
	wordsSpace = new SemanticSpace(allWords, vecs);

	
        //System.out.println(wordsSpace.getVector("the"));
        
        HashMap<String, HashMap<String, String>> sentences_id = IOUtils.readFastMappingPassages("/home/angeliki/sas/visLang/fast-mapping/create_jobs/CFsets_nonce-1.csv.ready.bold.withcos.untok");
        String[] sentences = new String[sentences_id.size()];
        String[] ids = new String[sentences_id.size()];
        foils = new String[sentences_id.size()];
        int i=0;
        for (String id : sentences_id.keySet()){
            sentences[i] = sentences_id.get(id).get("sentence");
	    foils[i] = sentences_id.get(id).get("foil");
            ids[i++] = id;
        }
        
        
        double[][] sentenceVector = new double[sentences.length][vecSize];
        for (int s=0;s<sentences.length;s++){
            String to_print = "";
 	    to_print +=ids[s]+"\t";
	    to_print +=foils[s]+"\t";
	 
  	    String foil = foils[s];
	    ArrayList<String> passage = new ArrayList<String>();
            for (String w:sentences[s].split(" ")){
                passage.add(w);
                to_print+=w+"#"+String.valueOf(wordsSpace.getSim(w,foil))+" ";
            }
            SimpleMatrix mat1;
            mat1 = SimpleMatrixUtils.sumRows(new SimpleMatrix(wordsSpace.getSubSpace(passage).getVectors()));
            mat1 = mat1.divide(passage.size());
            sentenceVector[s] = SimpleMatrixUtils.to2DArray(mat1)[0];
            System.out.println(to_print);            
        }
        
       
       
        
       
        SemanticSpace space = new SemanticSpace(ids, sentenceVector);
        space.exportSpace("/home/angeliki/sas/visLang/fast-mapping/sddfs");
        
        
	
    }
}
