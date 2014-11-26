package utils;


import javax.swing.JFrame;
import org.ejml.simple.SimpleMatrix;
import common.HeatMapPanel;
import demo.TestConstants;

import space.CompositionSemanticSpace;

public class CompositionHeatmap {
    public static void main(String[] args) {
        String compSpaceFile = TestConstants.S_COMPOSITION_FILE;
        String construction = "VP VB NP";
//        String construction = "@NP JJ NN";
//        String construction = "NP JJ NN";
//        String construction = "S NP VP";
        CompositionSemanticSpace space = CompositionSemanticSpace.loadCompositionSpace(compSpaceFile, true);
        SimpleMatrix compMatrix = space.getConstructionMatrix(construction);
        int width = compMatrix.numCols();
        System.out.println(width);
        int height = compMatrix.numRows();
        System.out.println(height);
        System.out.println(compMatrix.extractMatrix(0, 10, 0, 10));
        System.out.println(compMatrix.extractMatrix(0, 10, width/ 2, width/ 2 + 10));
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new HeatMapPanel(compMatrix));
        f.setSize(compMatrix.numCols() * 12, compMatrix.numRows() * 12);
        f.setLocation(200,200);
        f.setVisible(true);
        
    }
}
