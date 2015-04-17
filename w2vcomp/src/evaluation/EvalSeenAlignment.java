package evaluation;


import java.io.FileNotFoundException;
import java.util.HashSet;




import demo.TestConstants;

import space.Neighbor;
import space.SemanticSpace;
import word2vec.Images;

public class EvalSeenAlignment {
    
    
    public double[] evalRankAggr(SemanticSpace space, SemanticSpace searchSpace, HashSet<String> testWords){
        double[] ranks= new  double[] {0,0,0,0,0,0 };
        
        
       
        System.out.println("Size of search space "+searchSpace.getWords().length);
        
        int numOfNs = 100;
        int rank = 0;
        
        for (String word: testWords){
            Neighbor[] NNs = space.getNeighbors(word, numOfNs,  searchSpace);
            
            
            
            for (int i=0;i<numOfNs;i++){
               
                if (NNs[i].word.equals(word)){
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
        
        System.out.println("Mean:"+rank/(double) testWords.size());
        for (int i=0;i<ranks.length;i++){
            ranks[i] /= (double) testWords.size();
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
        
        
        HashSet<String> common_elements = new HashSet<String>(wordSpace.getWord2Index().keySet());
        common_elements.retainAll(visionSpace.getWord2Index().keySet());
        System.out.println("Test Alignment on seen data "+common_elements.size());
        
        
               
        
        
        EvalSeenAlignment eval = new EvalSeenAlignment();
        double[] ranks = eval.evalRankAggr(wordSpace, visionSpace, common_elements);
        eval.printRanks(ranks);
    }

}