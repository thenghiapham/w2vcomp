package evaluation;


import java.io.FileNotFoundException;
import java.util.ArrayList;
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
        
        
       
        System.out.println("Size of search space "+searchSpace.getWords().length);
        
        int numOfNs = 100;
        int rank = 0;
        System.out.println(this.words.size());
        String object;
        for (String word: this.words){
            object = this.goldStandard.get(word);
            Neighbor[] NNs = space.getNeighbors(word, numOfNs,  searchSpace);
            System.out.println(word+" ---->"+object+" ---->"+NNs[0].word);
            
            
            for (int i=0;i<numOfNs;i++){
                
                if (NNs[i].word.equals(object)){
                    rank+=i;
                    if (i==0){
                        //System.out.println(word+" ---->"+NNs[0].word+" "+NNs[0].sim);

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
            
        }
        
        System.out.println("Mean:"+rank/(double) this.words.size());
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
        Images im = new Images(TestConstants.VISION_FILE, true,TestConstants.imageDimensions);
        SemanticSpace visionSpace = im.getVisionSpace();
        
        EvalRetrieval eval  = new EvalRetrieval();
        eval.readGoldStandard(TestConstants.ROOT_EXP_DIR+"/corpus/Frank/dictionary.txt");
        
        
     
        double[] ranks = eval.evalRankAggr(wordSpace, visionSpace);
        eval.printRanks(ranks);
    }

}