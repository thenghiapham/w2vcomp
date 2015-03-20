package demo;


import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

import space.SemanticSpace;

public class SentenceAveraging {
    public static void main(String[] args) throws IOException {
        String wordvecs = "/home/angeliki/sas/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z_n5_m0.5_5_r11.0_r220.0l1.0E-4.bin";
        

        
        
        
        
        String task = "w2v";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("/home/angeliki/sas/visLang/fast-mapping/w2vPredictions.txt"));
        BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter("/home/angeliki/sas/visLang/fast-mapping/LinguisticPredictions.txt"));
        System.out.println("Reading Image Space...");
        
        
    	//import v2w mapping	
    	SimpleMatrix v2w = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream("/home/angeliki/sas/visLang/fast-mapping/crossmodal_general_training/training_data/cnn_svd/mapping/funtionFile/v2w.txt")), false));
    	SimpleMatrix w2v = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream("/home/angeliki/sas/visLang/fast-mapping/crossmodal_general_training/training_data/cnn_svd/mapping/funtionFile/w2v.txt")), false));
        
    	
    	//import visual space for foils and maybe map them in word space
    	SemanticSpace foilsSpace = SemanticSpace.importSpace("/home/angeliki/sas/visLang/fast-mapping/feature_extraction/cnn/foils/PER_SS.foils.cnn.cnn.features_svd300.dm");
    	SemanticSpace wordsSpace = SemanticSpace.readSpace(wordvecs);
    	
    	//map spaces
    	if (task.equals("v2w")){
    	    foilsSpace = new SemanticSpace(foilsSpace.getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(foilsSpace.getVectors())).mult(v2w)));
            
    	}
   
	
    	//read in chimeric data
    	HashMap<String, HashMap<String, String>> sentences_id = IOUtils.readFastMappingPassages("/home/angeliki/sas/visLang/fast-mapping/create_jobs/CFsets_nonce-1.csv.ready.bold.withcos.untok");
        
        String[] sentences = new String[sentences_id.size()];
        String[] ids = new String[sentences_id.size()];
        String[] foils = new String[sentences_id.size()];
        double[] mc = new double[sentences_id.size()];
        
        double[] passageLength = new double[sentences_id.size()];

        
        double[] sims = new double[sentences_id.size()];
        double[] simsLing = new double[sentences_id.size()];
        
        
        int i=0;
        //get some info
        for (String id : sentences_id.keySet()){
            sentences[i] = sentences_id.get(id).get("sentence");
            foils[i] = sentences_id.get(id).get("foil");
            mc[i] = Double.parseDouble(sentences_id.get(id).get("McRaeSim"));
            ids[i++] = id;
        }
            
        
        //compose sentences by averaging
        HashMap<Double, Double> sanityCheck = new HashMap<Double, Double>(); 
        
        for (int s=0;s<sentences.length;s++){
            
            String[] allSentences = sentences[s].split("@@");
            
            //what is the passage length
            passageLength[s] = Integer.valueOf(ids[s].split("_")[1].substring(1, 2));  //2,4, or 6
            
            
            sims[s] = 0;
            simsLing[s]  = 0;
            for (String sentence :allSentences){
        	    ArrayList<String> passage = new ArrayList<String>();
        	    
                for (String w:sentence.split(" ")){
                    passage.add(w);
                    
                }
                SemanticSpace passageSpace = wordsSpace.getSubSpace(passage);
                
                //compose sentence
                double[] sentenceVect = SimpleMatrixUtils.to2DArray((SimpleMatrixUtils.sumRows(new SimpleMatrix(passageSpace.getVectors())).divide(passageSpace.getWords().length)))[0];
                
                //create semantic space of vector
                String[] tmpwords = {"bla"};
                double[][] tmpm = {sentenceVect};
                
                SemanticSpace tmp = new SemanticSpace(tmpwords, tmpm);
                
                
                
                if (task.equals("w2v")){
                    simsLing[s] += tmp.getSim("bla",foils[s],wordsSpace);
                    tmp = new SemanticSpace(tmp.getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(tmp.getVectors())).mult(w2v)));
                   
                }
                
                sims[s] += tmp.getSim("bla",foils[s],foilsSpace);
                
                
            }
            //normalize by the num of sentences
            //write to file
            sims[s]/=allSentences.length;
            bufferedWriter.write(ids[s]+" "+sims[s]+"\n");
            if (task.equals("w2v")){
                simsLing[s] /=allSentences.length;
                bufferedWriter2.write(ids[s]+" "+simsLing[s]+"\n");
            }
                       
            
            if (!sanityCheck.containsKey(passageLength[s])){
                sanityCheck.put(passageLength[s], 0.0);
            }
            sanityCheck.put(passageLength[s],sanityCheck.get(passageLength[s])+sims[s]);
            
        }
        bufferedWriter.close();
        bufferedWriter2.close();
        
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        SpearmansCorrelation spearman = new SpearmansCorrelation();
                
        System.out.println(pearson.correlation(sims, passageLength));
        System.out.println(pearson.correlation(sims, mc));
        
        System.out.println(spearman.correlation(sims, passageLength));
        System.out.println(spearman.correlation(sims, mc));
        
        System.out.println(pearson.correlation(simsLing, passageLength));
        System.out.println(pearson.correlation(simsLing, mc));
        
        System.out.println(spearman.correlation(simsLing, passageLength));
        System.out.println(spearman.correlation(simsLing, mc));
        
        
        for (double l: sanityCheck.keySet()){
            System.out.println(l+" "+sanityCheck.get(l)/(sentences_id.size()/3));
            System.out.println(sentences_id.size()/3);
        }
        //space.exportSpace("/home/angeliki/sas/visLang/fast-mapping/sddfs");
        
        
	
    }
}
