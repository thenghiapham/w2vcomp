package demo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

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
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping/out_enwiki9_mini_1_r4cos.bin");
        SemanticSpace wordSpace2 = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping/out_enwiki9_0.bin");
        SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        
        System.out.println(wordSpace.getVectorSize());
        System.out.println(visionSpace.getVectorSize());
        String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping/out_enwiki9_mini_1_r4cos.txt";
        
        SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        SemanticSpace wordSpaceMapped = new SemanticSpace(wordSpace.getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(wordSpace.getVectors())).mult(map)));
        
        //System.out.println(map);
        
        Set<String> allConcepts = new HashSet<String>(visionSpace.getWord2Index().keySet());
        allConcepts.retainAll(wordSpace.getWord2Index().keySet());
     
        
        Set<String> trConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
        Set<String> tsConcepts = new HashSet<String>(allConcepts);
        tsConcepts.removeAll(trConcepts);
        Set<String> words = MenCorrelation.readWords(TestConstants.CCG_MEN_FILE, visionSpace,2);
        System.out.println(words.size());
        for (String word: words){
            System.out.print(word+":");
            if (wordSpace.getNeighbors(word, 10)!=null){
                for (Neighbor s: wordSpace.getNeighbors(word,5)){
                    System.out.print(s.word+" ");
                }
            }
            if (wordSpaceMapped.getNeighbors(word, 10)!=null){
                System.out.print("@@Second SpaceN@@ ");
                for (Neighbor s: wordSpaceMapped.getNeighbors(word, 5)){
                    System.out.print(s.word+" ");
                }
            }
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
