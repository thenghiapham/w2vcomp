package evaluation;

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

public class CompareVisualVec {
    
    

 

    public static void main(String[] args) throws FileNotFoundException {
        
       
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE,3);
        MenCorrelation sim999abs = new MenCorrelation(TestConstants.SIMLEX_FILE,3,6,1);
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        Images im = new Images(TestConstants.VISION_FILE, true);
        double m =  men.evaluateSpaceSpearman(im.getVisionSpace());
       
        System.out.println(m);
        
        
    

    }

}

