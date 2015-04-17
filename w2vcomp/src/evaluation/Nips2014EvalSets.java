package evaluation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

import common.HeatMapPanel;
import common.IOUtils;
import common.MenCorrelation;
import common.SimpleMatrixUtils;

import demo.TestConstants;
import space.SemanticSpace;
import word2vec.Images;

public class Nips2014EvalSets {
    
    

 

    public static void main(String[] args) throws FileNotFoundException {
        //SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/cross-situational/experiments/vectors/cds_n10_m0.5_10_r11.0_r220.0l1.0E-4.bin");
        SemanticSpace wordSpace = SemanticSpace.readSpace(TestConstants.VECTOR_FILE);
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE,3);
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        Images im = new Images(TestConstants.VISION_FILE, true, TestConstants.imageDimensions);
        
        
        
        HashSet<String> common_elements = new HashSet<String>(wordSpace.getWord2Index().keySet());
        common_elements.retainAll(im.getVisionSpace().getWord2Index().keySet());
        
        
        double[] cors = im.pairwise_cor(wordSpace);
        
        System.out.println("=========> COMMON "+common_elements.size()+"<==============");
        
        System.out.println("****LANGUAGE SPACE****");
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,common_elements, 1)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,common_elements,1)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,common_elements,1)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,common_elements,1 )); 
        System.out.println();
        
        
        System.out.println("****VISION SPACE****");
       

        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(im.getVisionSpace(),common_elements, 1)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(im.getVisionSpace(),common_elements,1)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(im.getVisionSpace(),common_elements,1)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(im.getVisionSpace(),common_elements,1 )); 
        System.out.println();
        
        System.out.println("=========> ALL LANGUAGE "+wordSpace.getWord2Index().size()+"<==============");
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman(wordSpace )); 
        
        
        System.out.println("=========> ALL VISION "+im.getVisionSpace().getWord2Index().size()+"<==============");
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman(im.getVisionSpace())); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman(im.getVisionSpace())); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman(im.getVisionSpace())); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman(im.getVisionSpace() )); 
        
        
        
        
        
        System.out.println("Printing pearson "+cors[0]);
        System.out.println("Printing spearman "+cors[1]);
        
       
        
       

    }

}

