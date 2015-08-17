package evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import common.IOUtils;

import space.SemanticSpace;
import word2vec.Images;
import demo.TestConstants;

public class EvalFScore {

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
    
    public static void main(String[] args) throws IOException {
<<<<<<< HEAD
        SemanticSpace wordSpace = SemanticSpace.readSpace(TestConstants.VECTOR_FILE);
        //SemanticSpace wordSpace = SemanticSpace.readSpace(TestConstants.ROOT_EXP_DIR+"experiments/vectors/"+"best_model_frank_random.bin");
        
=======
        //String TestFile = "/home/angeliki/Documents/cross-situational/experiments/vectors/best_model_frank.bin";
        String TestFile = "/home/angeliki/Documents/cross-situational/experiments/vectors/best_model_frank.bin";
        SemanticSpace wordSpace = SemanticSpace.readSpace(TestFile);
>>>>>>> refs/remotes/origin/crossSituational
        Images im = new Images(TestConstants.VISION_FILE, true,TestConstants.imageDimensions);

        SemanticSpace visionSpace = im.getVisionSpace();
        
        EvalFScore eval = new EvalFScore();
        eval.readGoldStandard(TestConstants.ROOT_EXP_DIR+"/corpus/Frank/dictionary.txt");
        
        eval.words.retainAll(wordSpace.getWord2Index().keySet());
        eval.objects.retainAll(visionSpace.getWord2Index().keySet());
        
        String[] words = new String[eval.words.size()];
        int kk=0;
        for (String word: eval.words){
            words[kk] = word;
            kk++;
        }
        String[] objects = new String[eval.objects.size()];
        kk=0;
        for (String word: eval.objects){
            objects[kk] = word;
            kk++;
        }
        
        
        double[][] cost = new double[eval.words.size()][eval.objects.size()];
        System.out.println(eval.words.size());
        for (int i = 0; i < eval.words.size(); i++)
        {
            for (int j = 0; j < eval.objects.size(); j++)
            {
                cost[i][j] = wordSpace.getSim(words[i], objects[j], visionSpace);
                
                
            }
        }
        System.out.println(cost.length+" "+cost[0].length);
        System.out.println(words.length+" "+objects.length);
        
        CompareAssociations acc = new CompareAssociations(words, objects,cost,eval.goldStandard);
        acc.fScore(null, "bla");
        
        System.out.println("SCORE: "+acc.score()+" out of "+words.length);
        acc.showBest();
    }

}
