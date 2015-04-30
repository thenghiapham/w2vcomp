package naacl2015Evals;

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
import word2vec.Images;

public class PrintNeighbors {

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        
        //SimpleMatrix pretrained = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream("/home/angeliki/sas/visLang/mmskipgram/out/fast-mapping/hs/out_3b_z_n5_m0.5_5_r11.0_r220.0l1.0E-4.mapping")), true));
        //System.out.println(pretrained);
        //IOUtils.saveMatrix("/home/angeliki/sas/visLang/mmskipgram/out/fast-mapping/hs/out_3b_z_n20_m0.5_20_r11.0_r21.0l1.0E-4.bin", SimpleMatrixUtils.to2DArray(pretrained),false);
        
        SemanticSpace bla = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.bin");
       
        bla.exportSpace("/home/angeliki/sas/forEnrica/multimodal_skipgram_withMapping.txt");
        /*SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        SemanticSpace t = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        Images im = new Images(TestConstants.VISION_FILE, true);
        wordSpace.getSubSpace(t.getWord2Index().keySet()).exportSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.txt");

        SemanticSpace visionSpace = im.getVisionSpace();
        SemanticSpace wordSpaceMapped;
        
        String[] words = {"make-up", "school","road","threat","potential"};
        
        
        //wordSpaceMapped = wordSpace;
        String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.mapping";
        SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        
        Set<String> toMap = new HashSet<String>();
        for (String word:words){
            toMap.add(word);
        }
        wordSpaceMapped = new SemanticSpace(wordSpace.getSubSpace(toMap).getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(wordSpace.getSubSpace(toMap).getVectors())).mult(map)));
        
       
      
     
        
        
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
        */
    }

}
