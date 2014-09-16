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

import space.SemanticSpace;
import vocab.Vocab;


public class Images {

    protected int[]         randomTable;
    private Random          random;
    protected int           randomTablesize;
    SemanticSpace           space;
    HashMap<String, Integer> word2Index;
    
    
    public Images(String textFile) {
        random = new Random();

        this.space = SemanticSpace.importSpace(textFile);
        this.word2Index = space.getWord2Index();
        random_vecs();
        //shuffling_vecs();

        this.randomTablesize = this.word2Index.size();
        initImageTable();
        
    }
    
     
   
    
    
    /**
    * Create an image table to randomly generate an image. 
    */
   protected void initImageTable() {
       this.randomTable = new int[this.randomTablesize];
       for (int i=0;i<this.randomTablesize; i++){
           this.randomTable[i] = i;
       }
    }
   
   protected void  random_vecs(){
       double [][] rand_vec = new double[this.word2Index.size()][this.space.getVectorSize()];
       System.out.println(this.word2Index.size()+" "+this.space.getVectorSize());
       for (int i=0;i<this.word2Index.size();i++){
           for (int j=0;j<this.space.getVectorSize();j++){
               rand_vec[i][j] = random.nextDouble();
           }
           this.space = new SemanticSpace(this.space.getWords(),rand_vec);
       }
   }
   
   public void shuffling_vecs(){
       int[] pos = new int[word2Index.size()];  
       for (int i=0; i<word2Index.size(); i++) {
           pos[i] = i;
       }
       Collections.shuffle(Arrays.asList(pos));
       int i=0;
       for (String key : word2Index.keySet()) {
           word2Index.put(key,pos[i]);
           i++;
       }
   }
   
   public double[][] getVectors(){
       return this.space.getVectors();
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
    Set<String> common_elements = new HashSet<String>(word2Index.keySet());
    common_elements.retainAll(word2Index.keySet());
    ArrayList<String> list_of_els = new ArrayList<String>(common_elements);
    
    double[] cors = new double[2];
    PearsonsCorrelation pearson = new PearsonsCorrelation();
    SpearmansCorrelation spearman = new SpearmansCorrelation();
    
    int len = common_elements.size();
    System.out.println(len);
    double[] sims_1 = new double[len*(len-1)/2];
    double[] sims_2 = new double[len*(len-1)/2];
    int k=0;
    for (int i=0;i<len;i++){
        for (int j=i+1;j<len;j++){
            String word1 = list_of_els.get(i);
            String word2 = list_of_els.get(j);
            
            sims_1[k]  = space.getSim(word1, word2);
            sims_2[k]  = space2.getSim(word1, word2);
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
