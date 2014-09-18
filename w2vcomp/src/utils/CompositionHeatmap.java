package utils;


import javax.swing.JFrame;
import org.ejml.simple.SimpleMatrix;
import common.HeatMapPanel;

import space.CompositionSemanticSpace;

public class CompositionHeatmap {
    public static void main(String[] args) {
        String compSpaceFile = "/home/thenghiapham/work/project/mikolov/output/bnc.cmp3tft";
        String construction = "NP JJ NN";
        CompositionSemanticSpace space = CompositionSemanticSpace.loadCompositionSpace(compSpaceFile, true);
        SimpleMatrix compMatrix = space.getConstructionMatrix(construction);
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new HeatMapPanel(compMatrix));
        f.setSize(compMatrix.numCols() * 12, compMatrix.numRows() * 12);
        f.setLocation(200,200);
        f.setVisible(true);
        
    }
}
