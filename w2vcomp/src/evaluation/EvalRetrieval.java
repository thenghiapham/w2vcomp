package evaluation;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import common.IOUtils;




import demo.TestConstants;

import space.Neighbor;
import space.SemanticSpace;
import word2vec.Images;

public class EvalRetrieval {
    
    public HashMap<String, String> goldStandard;

    public Set<String> objects;
    
    public Set<String> words;
    

    public void readGoldStandard(String gFile){
        this.goldStandard = new HashMap<String, String>();
        this.words = new HashSet<String>();
        this.objects = new HashSet<String>();
        
        ArrayList<String> lines = IOUtils.readFile(gFile);
        for (String line:lines){
            String[] els = line.split("[\t| ]+");
            System.out.println("Reading "+els[0]+" translated to "+els[1]);
            
            this.goldStandard.put(els[0], els[1]);
            
            this.words.add(els[0]);
            this.objects.add(els[1]);
        }
       
    }
    
    
    public double[] evalRankAggr(SemanticSpace space, SemanticSpace searchSpace){
        double[] ranks= new  double[] {0,0,0,0,0,0 };
        
        ArrayList<Integer> median = new ArrayList<Integer>();
        
       
        
        
        //searchSpace = searchSpace.getSubSpace(this.objects);
        
        System.out.println("Size of search space "+searchSpace.getWords().length);
        
        int numOfNs =searchSpace.getWord2Index().size();
        System.out.println(this.words.size());
        String object;
        int w = 0;
        for (String word: this.words){
            object = this.goldStandard.get(word);
            Neighbor[] NNs = space.getNeighbors(word, numOfNs,  searchSpace);
            System.out.println(word+" ---->"+object+" ---->"+NNs[0].word);
            
            median.add(w,numOfNs);
            
            for (int i=0;i<numOfNs;i++){
                if (NNs[i].word.equals(object)){
                    System.out.println(word+"with r"+(i+1));
                    median.add(w,i+1);
                    
                    if (i==0){

                        ranks[0]+=1; ranks[1]+=1; ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i==1){
                        ranks[1]+=1; ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i<=10){
                        ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i<=20){
                        ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i<=50){
                        ranks[4]+=1; ranks[5]+=1; 
                        break;
                    }
                    if (i<=100){
                        ranks[5]+=1;
                        break;
                    }
                }
            }
            w +=1;
            
        }
        
        Collections.sort(median);
        double m;
        if (median.size() % 2 == 0){
            m = (median.get(median.size()/2) + median.get(median.size()/2 -1))/2; 
            
        }
        else{
            m = median.get(median.size()/2);
        }
        
        System.out.println("Median:"+m);
        for (int i=0;i<ranks.length;i++){
            ranks[i] /= (double) this.words.size();
        }
        
        return ranks;
        
    }
    
    
    
    
    private void printRanks(double [] ranks){
        System.out.println("P@1: "+ranks[0] * 100+"%");
        System.out.println("P@2: "+ranks[1] * 100+"%");
        System.out.println("P@10: "+ranks[2] * 100+"%");
        System.out.println("P@20: "+ranks[3] * 100+"%");
        System.out.println("P@50: "+ranks[4] * 100+"%");


    }

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        
        

        SemanticSpace wordSpace = SemanticSpace.readSpace(TestConstants.VECTOR_FILE);
       // String TestFile = "//home/aggeliki/visLang/cross-situational/experiments/vectors/tuning/tune_d_200_n40_m0.2_r110.0_r20.05l1.0E-4.bin_normal";
        //String TestFile = "/home/aggeliki/visLang/cross-situational/experiments/vectors/simulations/frank_d_200_n40_m0.2_r110.0_r20.05l1.0E-4_attention.bin";
        //SemanticSpace wordSpace = SemanticSpace.readSpace(TestFile);
        Images im = new Images(TestConstants.VISION_FILE, true,TestConstants.imageDimensions);
        SemanticSpace visionSpace = im.getVisionSpace();
        
        EvalRetrieval eval  = new EvalRetrieval();
        eval.readGoldStandard(TestConstants.ROOT_EXP_DIR+"/corpus/frank/dictionary.txt");
        
        
     
        double[] ranks = eval.evalRankAggr(wordSpace, visionSpace);
        eval.printRanks(ranks);
    }

}