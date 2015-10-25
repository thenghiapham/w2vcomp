package word2vec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

import demo.TestConstants;

import space.SemanticSpace;
import vocab.Vocab;


public class Images {

    protected int[]         randomTable;
    private Random          random;
    protected int           randomTablesize;
    SemanticSpace           space;
    HashMap<String, Integer> word2Index;
    
    
    
    public Images(String textFile,boolean all) {
        random = new Random();

        this.space = SemanticSpace.importSpace(textFile);
        
        
        //Disregard some visual concepts.
        if (!all){
            String randomName = "NEGATIVEEXAMPLE@@";
            
            //Concepts that will be used for training
            Set<String> trConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
            SimpleMatrix A = new SimpleMatrix(this.space.getSubSpace(trConcepts).getVectors());
            //System.err.println("Only using labels for "+A.numRows()+" words");
            
            //Concepts that will be used for testing
            Set<String> tsConcepts = new HashSet<String>(this.space.getWord2Index().keySet());
            tsConcepts.removeAll(trConcepts);
            SimpleMatrix B = new SimpleMatrix(this.space.getSubSpace(tsConcepts).getVectors());
            
            
            ArrayList<String> rows = new ArrayList<String>();
            for (String w: trConcepts){
                rows.add(w);
            }
            for (String w: tsConcepts){
                rows.add(randomName.concat(w));
            }
            
            SimpleMatrix APLUSB = SimpleMatrixUtils.vStack(A, B);
            this.space = new SemanticSpace(rows, SimpleMatrixUtils.to2DArray(APLUSB));
            
        }
        
        //System.err.println("Size of space "+this.space.getVectorSize()+" "+this.space.getWord2Index().size());
        
        //how to reduce the dimensions
        double [][] newvecs = chop_summing();
        
        this.space = new SemanticSpace(this.space.getWords(), newvecs);
        //System.err.println("Use "+space.getWord2Index().keySet().size()+" image concepts for training with "+this.space.getVectorSize() );
        //System.err.println(new SimpleMatrix(this.space.getVectors()));
        
        this.word2Index = space.getWord2Index();
        //random_vecs();
        //System.err.println("Shuffling vecs");
        //shuffling_vecs();
        //System.err.println(this.space.getVectorSize());

        this.randomTablesize = this.word2Index.size();
        initImageTable();
        
    }
    
    
    
    public Images(String textFile,boolean all, int dimensions) {
        random = new Random();

        this.space = SemanticSpace.importSpace(textFile);
        if (!all){
            String randomName = "NEGATIVEEXAMPLE@@";
            
            Set<String> trConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
            SimpleMatrix A = new SimpleMatrix(this.space.getSubSpace(trConcepts).getVectors());
            
            Set<String> tsConcepts = new HashSet<String>(this.space.getWord2Index().keySet());
            tsConcepts.removeAll(trConcepts);
            SimpleMatrix B = new SimpleMatrix(this.space.getSubSpace(tsConcepts).getVectors());
            //System.err.println("Only using labels for "+A.numRows()+" words");
            
            ArrayList<String> rows = new ArrayList<String>();
            for (String w: trConcepts){
                rows.add(w);
            }
            for (String w: tsConcepts){
                rows.add(randomName.concat(w));
            }
            
            SimpleMatrix APLUSB = SimpleMatrixUtils.vStack(A, B);
            this.space = new SemanticSpace(rows, SimpleMatrixUtils.to2DArray(APLUSB));
            
        }
        //for mapping debugging
        
        double [][] newvecs = chop_summing();
        
        this.space = new SemanticSpace(this.space.getWords(), newvecs);
        //System.err.println("Use "+space.getWord2Index().keySet().size()+" image concepts for training with "+this.space.getVectorSize() );
        
        this.word2Index = space.getWord2Index();
        
        //For test only. Either randomly shuffle the words with the images or assign random vectors to words
        //random_vecs();
        //System.out.println("Shuffling vecs");
        //shuffling_vecs();
       
           
        //System.err.println("Images vectors with size:"+this.space.getVectorSize());

        //create hash that maps an int to a image ID for finding negative samples.
        this.randomTablesize = this.word2Index.size();
        initImageTable();
        
    }
    
     
   protected double[][] chop_randomly(){
       //System.err.println("Random Chopping");
       ArrayList<Integer> indices= new ArrayList<Integer>();
       for (int i=0;i<this.space.getVectorSize();i++){
           indices.add(i);
       }
       Collections.shuffle(indices);
       
       double [][] newvecs = new double[this.space.getWords().length][TestConstants.imageDimensions];
       int i = 0;
       for (double [] vec: this.space.getVectors()){
          for (int j=0; j<TestConstants.imageDimensions; j++){
              newvecs[i][j]= vec[indices.get(j)];
          }
          i++;
       }
       return newvecs;
       
   }
   
   
   protected double[][] chop_randomly(int dim){
       //System.err.println("Random Chopping");
       ArrayList<Integer> indices= new ArrayList<Integer>();
       for (int i=0;i<this.space.getVectorSize();i++){
           indices.add(i);
       }
       Collections.shuffle(indices);
       
       double [][] newvecs = new double[this.space.getWords().length][TestConstants.imageDimensions];
       int i = 0;
       for (double [] vec: this.space.getVectors()){
          for (int j=0; j<dim; j++){
              newvecs[i][j]= vec[indices.get(j)];
          }
          i++;
       }
       return newvecs;
       
   }
   
   protected double[][] chop_summing(){
       //System.err.println("Summed Chopping vectors of size "+this.space.getVectorSize());
      
       
       double [][] newvecs = new double[this.space.getWords().length][TestConstants.imageDimensions];
       for (int i=0; i<newvecs.length; i++){
           for (int j=0; j<newvecs[0].length; j++){
               newvecs[i][j]= 0;
           }
       }
       int i = 0;
       int offset = (int) Math.floor(this.space.getVectorSize()/TestConstants.imageDimensions);
       //System.err.println(offset);
       int cur = 0;
       int k=0;
       for (double [] vec: this.space.getVectors()){
          k = 0;
          cur =0;
          for (int j=0; j<this.space.getVectorSize(); j++){
              if (cur==offset && k!= TestConstants.imageDimensions-1){
                  newvecs[i][k]/= (double) cur;
                  k +=1;
                  cur = 0;
                  
              }
              cur += 1;
              newvecs[i][k]+= vec[j];
          }
          newvecs[i][k]/= (double) cur;
          i++;
          
       }
       return newvecs;
       
   }
    
    
   protected double[][] chop_linear(){
       //System.err.println("Linear Chopping");

       double [][] newvecs = new double[this.space.getWords().length][TestConstants.imageDimensions];
       int i = 0;
       for (double [] vec: this.space.getVectors()){
          for (int j=0; j<TestConstants.imageDimensions; j++){
              newvecs[i][j]= vec[j];
              
          }
          i++;
       }
       return newvecs;
   }
   
    /**
    * Create an image table to randomly generate a negative sample for an  image. 
    */
   protected void initImageTable() {
       this.randomTable = new int[this.randomTablesize];
       for (int i=0;i<this.randomTablesize; i++){
           this.randomTable[i] = i;
       }
       
       
       
    }
   
   protected void  random_vecs(){
       double [][] rand_vec = new double[this.word2Index.size()][this.space.getVectorSize()];
       //System.err.println(this.word2Index.size()+" "+this.space.getVectorSize());
       for (int i=0;i<this.word2Index.size();i++){
           for (int j=0;j<this.space.getVectorSize();j++){
               rand_vec[i][j] = random.nextDouble();
           }
           this.space = new SemanticSpace(this.space.getWords(),rand_vec);
       }
   }
   
   public void shuffling_vecs(){
       ArrayList<Integer> pos = new ArrayList<Integer>();  
       for (int i=0; i<word2Index.size(); i++) {
           pos.add(i, i);
       }
       Collections.shuffle(pos, new Random(TestConstants.SEED));
       //System.err.println("10 is "+pos.get(9));
       int i=0;
       for (String key : word2Index.keySet()) {
           word2Index.put(key,pos.get(i));
           i++;
       }
   }
   
   public double[][] getVectors(){
       return this.space.getVectors();
   }
   
   public Random getRandom(){
       return this.random;
   }
   
   public int randomWordIndex() {
       int randomInt = random.nextInt(this.randomTablesize);
       return this.randomTable[randomInt];
   }
   
   public int getIndex(String word){
       if (this.word2Index.containsKey(word)){
           return this.word2Index.get(word); 
       }
       else{
           return -1;
       }
       
   }
   
   public double[] pairwise_cor(SemanticSpace space2){
    Set<String> common_elements = new HashSet<String>(space2.getWord2Index().keySet());
    common_elements.retainAll(word2Index.keySet());
    ArrayList<String> list_of_els = new ArrayList<String>(common_elements);
    
    
    double[] cors = new double[2];
    PearsonsCorrelation pearson = new PearsonsCorrelation();
    SpearmansCorrelation spearman = new SpearmansCorrelation();
    
    int len = common_elements.size();
    //System.err.println("Correlating "+len+" elements");
    double[] sims_1 = new double[len*(len-1)/2];
    double[] sims_2 = new double[len*(len-1)/2];
    int k=0;
    for (int i=0;i<len;i++){
        for (int j=i+1;j<len;j++){
            String word1 = list_of_els.get(i);
            String word2 = list_of_els.get(j);
            
            sims_1[k]  = space.getSim(word1, word2);
            sims_2[k]  = space2.getSim(word1, word2);
            if (Double.isNaN(sims_1[k]) || Double.isNaN(sims_2[k]) || sims_1[k]==0 || sims_2[k]==0){
                //System.out.println(word1+" "+word2+" "+sims_1[k]+" "+sims_2[k]);
            }
            
            
            k+=1;
            
        }
    }
   
    cors[0] = spearman.correlation(sims_1, sims_2);   
    cors[1] = pearson.correlation(sims_1, sims_2); 
    
    
    return cors;
       
   }
   
   
   
   public SemanticSpace getVisionSpace(){
       return this.space;
   }

}
