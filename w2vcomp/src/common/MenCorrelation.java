package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import demo.TestConstants;

import space.SemanticSpace;

/**
 * This class can be used to evaluate a word vector space by computing the
 * correlation between the cosine of the words' vectors and the gold-standard
 * similarities of them (typically based on human judgment)
 * The name is kind of misleading since we can use other dataset than MEN
 * @author thenghiapham
 *
 */

public class MenCorrelation {
	String[][] wordPairs;
	Set<String> words;
	double[] golds;
	PearsonsCorrelation pearson;
	SpearmansCorrelation spearman;
	
	/**
	 * Initialize with the path to the dataset file
	 * @param dataset
	 */
	public MenCorrelation(String dataset) {
	    pearson = new PearsonsCorrelation();
	    spearman = new SpearmansCorrelation();
		readDataset(dataset);
	}
	
	public MenCorrelation(String dataset, int field) {
        pearson = new PearsonsCorrelation();
        spearman = new SpearmansCorrelation();
        readDataset(dataset,field);
    }
	
	public MenCorrelation(String dataset, int field, int field2,int filter) {
        pearson = new PearsonsCorrelation();
        spearman = new SpearmansCorrelation();
        readDataset(dataset,field, field2, filter);
    }
	
	
	
	
	public MenCorrelation(String[][] wordPairs, double[] golds) {
	    pearson = new PearsonsCorrelation();
        spearman = new SpearmansCorrelation();
        this.wordPairs = wordPairs;
        this.golds = golds;
	}
	

    /**
     * Read the word pairs and the gold standard from the dataset
     * @param dataset
     */
	public void readDataset(String dataset) {
		ArrayList<String> data = IOUtils.readFile(dataset);
		words = new HashSet<String>();
		
		
		golds = new double[data.size()];
		wordPairs = new String[data.size()][2];
		
		for (int i = 0; i < data.size(); i++) {
			String dataPiece = data.get(i);
			String elements[] = dataPiece.split("[ \t]+");
			
			
			wordPairs[i][0] = elements[0];
			wordPairs[i][1] = elements[1];
			words.add(wordPairs[i][0]);
			words.add(wordPairs[i][1]);
			
			
			golds[i] = Double.parseDouble(elements[2]);
		}
	}
	
	public Set<String> getWords(){
	    return this.words;
	}
	
	public static Set<String> readWords(String dataset, SemanticSpace space, int typeOfVec) {
        Set<String> words = space.getWord2Index().keySet();

	    
        ArrayList<String> data = IOUtils.readFile(dataset);
        Set<String> el = new HashSet<String>();
        for (int i = 0; i < data.size(); i++) {
            
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("[ \t]+");
            for (int j=0;j<2;j++){
                if ((typeOfVec==1 && words.contains(elements[j])) || (typeOfVec==2 && !words.contains(elements[j]))){
                    el.add(elements[j]);
                }
            }
       }
        
        return el;
    }
	
	/**
     * Read the word pairs and the gold standard from the dataset's
     * field field
     * @param dataset
     */
    public void readDataset(String dataset, int field) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        golds = new double[data.size()];
        wordPairs = new String[data.size()][2];
        words = new HashSet<String>();

        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("[ \t]+");
            wordPairs[i][0] = elements[0];
            wordPairs[i][1] = elements[1];
            words.add(wordPairs[i][0]);
            words.add(wordPairs[i][1]);
            golds[i] = Double.parseDouble(elements[field]);
        }
    }
	
    
    /**
     * Read the word pairs and the gold standard from the dataset's
     * field field
     * @param dataset
     */
    public void readDataset(String dataset, int field, int field2, int filter) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        ArrayList<String[]> temp1 = new ArrayList<String[]>();
        ArrayList<Double> temp2 = new ArrayList<Double>();
        words = new HashSet<String>();

        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("[ \t]+");
            if (Integer.parseInt(elements[field2]) == filter ){
                String[] t = new String[2];
                t[0]  = elements[0];
                t[1]  = elements[1];
                temp1.add(t);
                temp2.add(Double.parseDouble(elements[field]));
                
            }
        }
        
        golds = new double[temp1.size()];
        wordPairs = new String[temp1.size()][2];
        for (int i=0;i<golds.length; i++){
            String[] t = temp1.get(i);
            wordPairs[i][0] = t[0];
            wordPairs[i][1] = t[1];
            words.add(wordPairs[i][0]);
            words.add(wordPairs[i][1]);
            golds[i] = temp2.get(i);
        }
        
    }
    
	/**
	 * Compute the pearson correlation of the predicted values against the gold
	 * standard
	 * @param predicts
	 * @return
	 */
	public double pearsonCorrelation(double[] predicts) {
	    return pearson.correlation(golds, predicts);
	}
	
	/**
	 * Compute the spearman correlation of the predicted values against the gold
     * standard 
	 * @param predicts
	 * @return
	 */
	public double spearmanCorrelation(double[] predicts) {
        return spearman.correlation(golds, predicts);
    }
	
	
	/**
	 * Evaluate the space using the pearson correlation
	 * @param space
	 * @return
	 */
	public double evaluateSpacePearson(SemanticSpace space) {
	    double[] predicts = new double[golds.length];
	    for (int i = 0; i < golds.length; i++) {
	        predicts[i] = space.getSim(wordPairs[i][0], wordPairs[i][1]);
	    }
	    return pearson.correlation(golds, predicts);
	}
	
	/**
     * Evaluate the space using the spearman correlation
     * @param space
     * @return
     */
	public double evaluateSpaceSpearman(SemanticSpace space) {
	    int exists=0;
	    double[] predicts = new double[golds.length];
        for (int i = 0; i < golds.length; i++) {
            if (space.getSim(wordPairs[i][0], wordPairs[i][1])==0){
               continue;
            }
           
            exists++;
            predicts[i] = space.getSim(wordPairs[i][0], wordPairs[i][1]);
        }
        
        System.out.println(exists/ (double)golds.length+" are 0");
        return spearman.correlation(golds, predicts);
    }
	
	   /**
     * Evaluate the space using the spearman correlation
     * @param space
     * @return
     */
    public double evaluateSpaceSpearman(HashMap<String, HashMap<String, Double>> ratings) {
        ArrayList<Double> predicts = new ArrayList<Double>();
        ArrayList<Double> g  = new ArrayList<Double>();
        int exists=0;
        
        for (int i = 0; i < golds.length; i++) {
            if (!ratings.containsKey(wordPairs[i][0]) || !ratings.get(wordPairs[i][0]).containsKey(wordPairs[i][1])){
                continue;
            }
            predicts.add(ratings.get(wordPairs[i][0]).get(wordPairs[i][1]));
            g.add(golds[i]);
            exists++;
        }
        
        double[] p1 = new double[g.size()]; 
        double[] p2 = new double[g.size()]; 
        for (int i=0 ;i<g.size();i++){
            p1[i] = g.get(i);
            p2[i] = predicts.get(i);
        }
        System.out.println(exists);
        //System.out.println(exists/ (double)golds.length+" are 0");
        return spearman.correlation(p1,p2);
    }
	
	/**
     * Evaluate the space using the spearman correlation
     * @param space
     * @return
     */
    public double evaluateSpaceSpearman2(SemanticSpace space,SemanticSpace space2, int typeOfVec) {
        ArrayList<Double> predicts = new ArrayList<Double>();
        ArrayList<Double> g  = new ArrayList<Double>();
        Set<String> words = space2.getWord2Index().keySet();
        
        Set<String> tsConcepts = new HashSet<String>(words);
        tsConcepts.removeAll(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
        
       int exists = 0;

        for (int i = 0; i < golds.length; i++) {
            if (typeOfVec==1 && (!words.contains(wordPairs[i][0]) || !words.contains(wordPairs[i][1]))){
               
               continue;
            }
            if (typeOfVec==2 && (words.contains(wordPairs[i][0]) || words.contains(wordPairs[i][1]))){
                
                continue;
            }
            if (typeOfVec==3 && ( 
                        (words.contains(wordPairs[i][0]) &&  words.contains(wordPairs[i][1])) 
                            || ( !words.contains(wordPairs[i][0]) &&  !words.contains(wordPairs[i][1])) )){
                
                continue;
            }
            if (typeOfVec==4 && (!tsConcepts.contains(wordPairs[i][0]) || !tsConcepts.contains(wordPairs[i][1]))){
                continue;
            }
            

            if (space.getSim(wordPairs[i][0], wordPairs[i][1])==0){
                //System.out.println(0);
                continue;
            }
            predicts.add(space.getSim(wordPairs[i][0], wordPairs[i][1]));
            exists++;
            //if (typeOfVec==2 )
            //System.out.println(wordPairs[i][0]+" "+wordPairs[i][1]);
            g.add(golds[i]);
            
            
        }
        
        double[] p1 = new double[g.size()]; 
        double[] p2 = new double[g.size()]; 
        for (int i=0 ;i<g.size();i++){
            p1[i] = g.get(i);
            p2[i] = predicts.get(i);
        }
        System.out.println(exists);
        //System.out.println(exists/ (double)golds.length+" are 0");
        return spearman.correlation(p1,p2);
    }
    
    
    /**
     * Evaluate the space using the spearman correlation
     * @param space
     * @return
     */
    public double evaluateSpaceSpearman2(SemanticSpace space,HashSet<String> words, int typeOfVec) {
        ArrayList<Double> predicts = new ArrayList<Double>();
        ArrayList<Double> g  = new ArrayList<Double>();
        
        Set<String> tsConcepts = new HashSet<String>(words);
        
       int exists = 0;

        for (int i = 0; i < golds.length; i++) {
            if (typeOfVec==1 && (!words.contains(wordPairs[i][0]) || !words.contains(wordPairs[i][1]))){
               
               continue;
            }
            if (typeOfVec==2 && (words.contains(wordPairs[i][0]) || words.contains(wordPairs[i][1]))){
                
                continue;
            }
            if (typeOfVec==3 && ( 
                        (words.contains(wordPairs[i][0]) &&  words.contains(wordPairs[i][1])) 
                            || ( !words.contains(wordPairs[i][0]) &&  !words.contains(wordPairs[i][1])) )){
                
                continue;
            }
            if (typeOfVec==4 && (!tsConcepts.contains(wordPairs[i][0]) || !tsConcepts.contains(wordPairs[i][1]))){
                continue;
            }
            

            if (space.getSim(wordPairs[i][0], wordPairs[i][1])==0){
                //System.out.println(0);
                continue;
            }
            predicts.add(space.getSim(wordPairs[i][0], wordPairs[i][1]));
            exists++;
            //if (typeOfVec==2 )
            //System.out.println(wordPairs[i][0]+" "+wordPairs[i][1]);
            g.add(golds[i]);
            
            
        }
        
        double[] p1 = new double[g.size()]; 
        double[] p2 = new double[g.size()]; 
        for (int i=0 ;i<g.size();i++){
            p1[i] = g.get(i);
            p2[i] = predicts.get(i);
        }
        //System.out.println(exists);
        System.out.println(exists/ (double)golds.length+" are 0");
        return spearman.correlation(p1,p2);
    }
    
    
   
	
	/**
	 * @return the gold standard (human's judgment on the similarities)
	 */
	public double[] getGolds() {
	    return golds;
	}
	
	public static void main(String[] args) {
	    //SemanticSpace space = SemanticSpace.readSpace("/home/thenghiapham/svn/w2v-unmodified/vectors.bin");
	    SemanticSpace space = SemanticSpace.readSpace(TestConstants.VECTOR_FILE);
	    MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
	    System.out.println("men: " + men.evaluateSpacePearson(space));
	}
}
