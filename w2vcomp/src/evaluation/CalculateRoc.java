package evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import space.SemanticSpace;
import word2vec.Images;

import common.IOUtils;

import demo.TestConstants;

public class CalculateRoc {

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
        SemanticSpace wordSpace = SemanticSpace.readSpace(TestConstants.VECTOR_FILE);
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
        for (int i = 0; i < eval.words.size(); i++)
        {
            for (int j = 0; j < eval.objects.size(); j++)
            {
                cost[i][j] = wordSpace.getSim(words[i], objects[j], visionSpace);
                if (eval.goldStandard.get(words[i]).equals(objects[j])){
                    System.out.println(cost[i][j]+" 1");
                }
                else{
                    System.out.println(cost[i][j]+" 0");
                }
                
            }
        }
       
    }

}
