package naacl2015Evals;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/fast-mapping/3b/out_3b_z_n-1_m0.5_-1_r11.0_r21.0l1.0E-4.bin");
        
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE,3);
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        Images im = new Images(TestConstants.VISION_FILE, true);
        
        HashSet<String> common_elements = new HashSet<String>(IOUtils.readFile("/home/angeliki/Documents/mikolov_composition/misc/common_elements_alternative"));
        Set<String> testData = new HashSet<String>();
        testData.addAll(men.getWords());
        testData.addAll(sim999.getWords());
        testData.addAll(visSim.getWords());
        
        
        
       /* String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again/out_wiki_n5_m0.5_5_r11.0_r210.0l1.0E-4.mapping";
        SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        
        Set<String> toMap = new HashSet<String>(testData);
        toMap.retainAll(wordSpace.getWord2Index().keySet());
        SemanticSpace wordSpaceMapped = new SemanticSpace(wordSpace.getSubSpace(toMap).getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(wordSpace.getSubSpace(toMap).getVectors())).mult(map)));
        
        wordSpace = wordSpaceMapped;*/
        
        double[] cors = im.pairwise_cor(wordSpace);
        
       
        
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
        System.out.println("Printing pearson "+cors[0]);
        System.out.println("Printing spearman "+cors[1]);
        
       
        
       

    }

}

