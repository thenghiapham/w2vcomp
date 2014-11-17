package nips2014eval;

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
        //SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping_max/out_enwiki9_5_r11.0_r220.0l1.0E-6.bin");
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/masterclic4/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        
        SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE);
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        Images im = new Images(TestConstants.VISION_FILE, true);
        
        /*String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping_max/out_enwiki9_m0.5_5_r11.0_r220.0l1.0E-4.mapping";
        
        SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        map  = map.transpose();
        wordSpace = new SemanticSpace(wordSpace.getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(wordSpace.getVectors())).mult(map)));
        */
        
        double[] cors = im.pairwise_cor(wordSpace);
        
        //wordSpace = visionSpace;
        
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,visionSpace, 1)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,visionSpace,1)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,visionSpace,1)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,visionSpace,1 )); 
        System.out.println();
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,visionSpace,2)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,visionSpace,2)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,visionSpace,2)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,visionSpace,2 )); 
        System.out.println();
       
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,visionSpace,3)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,visionSpace,3)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,visionSpace,3)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,visionSpace,3 )); 
        System.out.println();
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman(wordSpace )); 
        System.out.println("Printing pearson "+cors[0]);
        System.out.println("Printing spearman "+cors[1]);
        
        /*String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping/out_enwiki9_1_5_0.5_cnn.txt";
        
        SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        System.out.println(map.numCols()+" "+ map.numRows());
        HeatMapPanel.plotHeatMap(map);
        //System.out.println(map);*/
        
       

    }

}

