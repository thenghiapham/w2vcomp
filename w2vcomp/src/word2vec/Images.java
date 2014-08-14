package word2vec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Collections;

import space.SemanticSpace;
import vocab.Vocab;


public class Images {

    protected int[]         randomTable;
    private Random          random;
    protected int           randomTablesize;
    SemanticSpace           space;
    HashMap<String, Integer> word2Index;
    
    
    public Images(String textFile) {
        this.space = SemanticSpace.importSpace(textFile);
        this.word2Index = space.getWord2Index();
        shuffling_vecs();

        this.randomTablesize = this.word2Index.size();
        initImageTable();
        random = new Random();
        
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

}
