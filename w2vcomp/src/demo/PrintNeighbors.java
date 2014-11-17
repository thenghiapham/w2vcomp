package demo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

import common.HeatMapPanel;
import common.IOUtils;
import common.MenCorrelation;
import common.SimpleMatrixUtils;

import space.Neighbor;
import space.SemanticSpace;

public class PrintNeighbors {

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping_max/out_enwiki9_m0.5_5_r11.0_r220.0l1.0E-4.bin");
        SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping_max/out_enwiki9_m0.5_5_r11.0_r220.0l1.0E-4.mapping";
        SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        //map  = map.transpose();

        SemanticSpace wordSpaceMapped = new SemanticSpace(wordSpace.getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(wordSpace.getVectors())).mult(map)));
//        HeatMapPanel.plotHeatMap(map);
//        SemanticSpace wordSpaceMapped = wordSpace;

       
        
        Set<String> allConcepts = new HashSet<String>(visionSpace.getWord2Index().keySet());
        allConcepts.retainAll(wordSpace.getWord2Index().keySet());
     
        
        Set<String> trConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
        Set<String> tsConcepts = new HashSet<String>(allConcepts);
        tsConcepts.removeAll(trConcepts);
        Set<String> words = MenCorrelation.readWords(TestConstants.Carina_FILE, visionSpace,1);
        System.out.println(words.size());
        for (String word: words){
            System.out.print(word+" --->");
            if (wordSpace.getNeighbors(word, 10)!=null){
                for (Neighbor s: wordSpace.getNeighbors(word,5)){
                    System.out.print(s.word+" ");
                }
            }
//            if (wordSpaceMapped.getNeighbors(word, 10)!=null){
//                System.out.print("@@Second SpaceN@@ ");
//                for (Neighbor s: wordSpaceMapped.getNeighbors(word, 5)){
//                    System.out.print(s.word+" ");
//                }
//            }
            if (wordSpace.getNeighbors(word, 5)!=null){
                System.out.print("@@VISION@@ ");
                for (Neighbor s: wordSpaceMapped.getNeighbors(word, 5,visionSpace)){
                    System.out.print(s.word+" ");
                }
            }
            System.out.println();
        }
    }

}
