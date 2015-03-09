package baselines;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import common.IOUtils;
import common.MenCorrelation;
import common.SimpleMatrixUtils;
import demo.TestConstants;

import space.SemanticSpace;
import word2vec.Images;

public class Concatenation {

    int k = 0;
    SemanticSpace data;
    int[] images_dim;
    int[] words_dim;
    
    public Concatenation(SemanticSpace vision,SemanticSpace language){
        createPairedData(vision, language);
    }
    
    
    //gets 2 spaces, find common elements, and creates paired data
    private void createPairedData(SemanticSpace vision, SemanticSpace language){
        
        Set<String> common_elements = new HashSet<String>(vision.getWord2Index().keySet());
        common_elements.retainAll(language.getWord2Index().keySet());
        
        ArrayList<String> el = new ArrayList<String>();
        for (String e:common_elements){
            el.add(e);
        }
        SimpleMatrix images = new SimpleMatrix(vision.getSubSpace(el).getVectors());
        SimpleMatrix words = new SimpleMatrix(language.getSubSpace(el).getVectors());
        
        images_dim = new int[2];
        images_dim[0] = images.numRows();
        images_dim[1] = images.numCols();
        
        words_dim = new int[2];
        words_dim[0] = words.numRows();
        words_dim[1] = words.numCols();
        
        this.data = new SemanticSpace(el,SimpleMatrixUtils.to2DArray(SimpleMatrixUtils.hStack(images, words)));
    }
    
    
    public SemanticSpace  getSpace(){
        
       return this.data;
}
    
    
    
   
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE,3);
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        MenCorrelation sim999abs = new MenCorrelation(TestConstants.SIMLEX_FILE,3,6,1);
        
        HashSet<String> common_elements = new HashSet<String>(IOUtils.readFile("/home/angeliki/Documents/mikolov_composition/misc/common_elements_alternative"));

        
        Set<String> testData = new HashSet<String>();
        testData.addAll(men.getWords());
        testData.addAll(sim999.getWords());
        testData.addAll(visSim.getWords());
        
        
        /*
        //for kiela
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        SemanticSpace visionSpace = SemanticSpace.importSpace("/home/angeliki/Documents/mikolov_composition/misc/kiela.txt");
        Concatenation baseline = new Concatenation(wordSpace.getSubSpace(testData).rowNormalize(), visionSpace.getSubSpace(testData).rowNormalize());
        wordSpace = baseline.getSpace();
        */
        
        /*
        //for bruni
        SemanticSpace wordSpace = SemanticSpace.importSpace("/home/angeliki/Documents/mikolov_composition/misc/window2textMixed_filtered.txt");
        */
        
        /*
        //for our concatenarion
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        SemanticSpace visionSpace = im.getVisionSpace();
        Images im = new Images(TestConstants.VISION_FILE, true);
        Concatenation baseline = new Concatenation(wordSpace.getSubSpace(testData).rowNormalize(), visionSpace.getSubSpace(testData).rowNormalize());
        wordSpace = baseline.getSpace();
        */
        
        /* 
        //for our Skip-gram
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        */
        
       
        //for our CNN features
        Images im = new Images(TestConstants.VISION_FILE, true);
        SemanticSpace wordSpace = im.getVisionSpace();
        
        
        System.out.println("Evaluating only common in all spaces elements");
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,common_elements, 1)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,common_elements,1)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,common_elements,1)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,common_elements,1 )); 
        System.out.println();
        
        
        
        System.out.println("Evaluating all elements");
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman(wordSpace )); 
     
        
     
        
        



        
      
        
        

    }

}
