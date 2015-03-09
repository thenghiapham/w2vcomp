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

public class CompareVisualVec {
    
    

 

    public static void main(String[] args) throws FileNotFoundException {
        
        double a;
        int p = 0;
        int t = 100;
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE,3);
        MenCorrelation sim999abs = new MenCorrelation(TestConstants.SIMLEX_FILE,3,6,1);
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        Images im = new Images(TestConstants.VISION_FILE, true);
        double m =  sim999.evaluateSpaceSpearman2(im.getVisionSpace(),im.getVisionSpace(), 1);
        
        for (int i=0;i<t;i++){
            im = new Images(TestConstants.VISION_FILE, true,300);
            a = sim999.evaluateSpaceSpearman2(im.getVisionSpace(),im.getVisionSpace(), 1);
            if (a>=m){
                p++;
            }
            System.out.println(p+" "+a+" "+m);
        }
        System.out.println((double) p/(double)t);
        
        
    

    }

}

