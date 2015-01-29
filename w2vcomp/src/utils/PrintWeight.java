package utils;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import demo.TestConstants;

import space.WeightedCompositionSemanticSpace;

public class PrintWeight {
    public static void main(String[] args) {
            String compSpaceFile = "/home/thenghiapham/work/project/mikolov/output/s_neg_4_w_lwiki_300_10_i_wo_lex.cmp";
    //        String construction = "VP VB NP";
    //        String construction = "NP NN NN";
    //        String construction = "NP NN NN";
    //        String construction = "NP JJ NN";
    //        String construction = "S NP VP";
            WeightedCompositionSemanticSpace space = WeightedCompositionSemanticSpace.loadCompositionSpace(compSpaceFile, true);
            ArrayList<String> a = IOUtils.readFile(TestConstants.S_CONSTRUCTION_FILE);
            for (String line: a) {
                if (line.startsWith("#")) continue;
                String[] elements = line.split("( |\t)");
                if (elements.length < 4) continue;
                String construction = elements[1] + " " +elements[2] + " " +elements[3];
                SimpleMatrix vector = space.getConstructionMatrix(construction);
                System.out.println(construction + ":    " + vector.get(0) + " " + + vector.get(1));
            }
            
            
        }

}
