package demo;

import java.util.HashSet;
import java.util.Set;

import common.IOUtils;

import space.Neighbor;
import space.SemanticSpace;

public class PrintNeighbors {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/out_enwik9_20.bin");
        SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        
        
        
        Set<String> allConcepts = new HashSet<String>(visionSpace.getWord2Index().keySet());
        allConcepts.retainAll(wordSpace.getWord2Index().keySet());
     
        
        Set<String> trConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
        Set<String> tsConcepts = new HashSet<String>(allConcepts);
        tsConcepts.removeAll(trConcepts);
        
        
        for (String word: tsConcepts){
            System.out.print(word+":");
            for (Neighbor s: wordSpace.getNeighbors(word, 10)){
                System.out.print(s.word+" ");
            }
            System.out.print("@@VISION@@ ");
            for (Neighbor s: wordSpace.getNeighbors(word, 10,visionSpace)){
                System.out.print(s.word+" ");
            }
            System.out.println();
        }
    }

}
