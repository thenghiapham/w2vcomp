package evaluation;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.IOUtils;




import demo.TestConstants;

import space.Neighbor;
import space.SemanticSpace;
import word2vec.Images;

public class EvalRetrievalGeneralization {
    
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
    
    
    public void evalRankAggr(SemanticSpace space, SemanticSpace searchSpace){
        double[] ranks= new  double[] {0,0,0,0,0,0 };
        
        HashMap<String, List<String>> toEval = new HashMap<String, List<String>>();
        toEval.put("bear", Arrays.asList(new String[] {"bear","bison","skunk","strawberry"}));
        toEval.put("book", Arrays.asList(new String[] {"book","brick","clock","tuna"}));
        toEval.put("cow", Arrays.asList(new String[] {"cow","bull","goose","garage"}));
        toEval.put("duck", Arrays.asList(new String[] {"duck","chicken","finch","bear"}));
        toEval.put("lamb", Arrays.asList(new String[] {"lamb","sheep","rabbit","broccoli"}));
        toEval.put("mirror", Arrays.asList(new String[] {"mirror","axe","shelves","squid"}));
        toEval.put("pig", Arrays.asList(new String[] {"pig","hamster","penguin","elevator"}));
        toEval.put("duck", Arrays.asList(new String[] {"cat","lamb","cat","carpet"}));

        
      
        System.out.println("Size of search space "+searchSpace.getWords().length);
        
        int numOfNs = 10;
        int rank = 0;
        System.out.println(this.words.size());
        String object;
        int mean_cor = 0;
        for (String word: this.words){
            object = this.goldStandard.get(word);
            if (!toEval.containsKey(object)){
                continue;
            }
            
            //reduce search space to relevant items
            Set<String> to_keep = new HashSet<String>();
            for (String o: searchSpace.getWords()){
                if (toEval.get(object).contains(o.split("@")[0])){
                    to_keep.add(o);
                }
            }
            SemanticSpace searchSpace2 = searchSpace.getSubSpace(to_keep);
            
            
            Neighbor[] NNs = space.getNeighbors(word, numOfNs,  searchSpace2);
            System.out.print(word+"("+object+"): ");
            
            int cor = 0;
            for (int i=0;i<numOfNs;i++){
                System.out.print(NNs[i].word.split("@")[0]+",");
                if (NNs[i].word.startsWith(object+"@")){
                    rank+=i;
                    cor+=1;
         
                }
            }
            mean_cor += cor;
            System.out.println(cor);
        }
        
        System.out.println("Mean:"+mean_cor/(double) this.words.size());
        
        
    }
    
    
    
    
        

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        String TestFile = TestConstants.ROOT_EXP_DIR+"experiments/vectors/simulations/frank_d_200_n40_m0.2_r110.0_r20.05l1.0E-4_attention.bin";
        //String TestFile = TestConstants.VECTOR_FILE;
        SemanticSpace wordSpace = SemanticSpace.readSpace(TestFile);

        Images im = new Images(TestConstants.ROOT_VISUAL_DIR+"extra_generalization_experiment/"+"fc7_space_10imagesPerObject_more.txt", true,TestConstants.imageDimensions); 
        SemanticSpace visionSpace = im.getVisionSpace();
        
        
        EvalRetrievalGeneralization eval  = new EvalRetrievalGeneralization();
        eval.readGoldStandard(TestConstants.ROOT_EXP_DIR+"/corpus/frank/dictionary.txt");
        
        
     
        eval.evalRankAggr(wordSpace, visionSpace);
    }

}