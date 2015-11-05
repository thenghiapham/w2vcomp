package evaluation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ejml.simple.SimpleMatrix;

import common.HeatMapPanel;
import common.IOUtils;
import common.MenCorrelation;
import common.SimpleMatrixUtils;
import demo.TestConstants;

import space.Neighbor;
import space.SemanticSpace;
import word2vec.Images;

public class PrintNeighbors {

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        
        
        String TestFile = "/home/aggeliki/visLang/cross-situational/experiments/vectors/simulations/frank_d_200_n40_m0.2_r110.0_r20.05l1.0E-4_attention.bin";

        SemanticSpace wordSpace = SemanticSpace.readSpace(TestFile);
        Images im = new Images(TestConstants.VISION_FILE, true,TestConstants.imageDimensions);

        SemanticSpace visionSpace = im.getVisionSpace();
        
        
        Set<String> common_elements = new HashSet<String>(wordSpace.getWord2Index().keySet());
        System.out.println(common_elements.size());
        common_elements.retainAll(visionSpace.getWord2Index().keySet());
        System.out.println(common_elements.size());
        
        ArrayList<String> common_elements_list = new ArrayList<String>(common_elements);
        ArrayList<String> list_of_els = new ArrayList<String>();
        
        list_of_els.add("music");
        list_of_els.add("baby");
        list_of_els.add("chair");
        list_of_els.add("balls");
        list_of_els.add("drummer");
        

        visionSpace = visionSpace.getSubSpace(common_elements_list);    
        for (String word: list_of_els){
            System.out.print(word+" --->");
            if (wordSpace.getNeighbors(word, 10)!=null){
                for (Neighbor s: wordSpace.getNeighbors(word,3)){
                    System.out.print(s.word+" ");
                }
            }

            if (wordSpace.getNeighbors(word, 5)!=null){
                System.out.print("@@VISION@@ ");
                for (Neighbor s: wordSpace.getNeighbors(word, 3,visionSpace)){
                    System.out.print(s.word+" ");
                }
            }
            System.out.println();
           
        }
        
    }

}
