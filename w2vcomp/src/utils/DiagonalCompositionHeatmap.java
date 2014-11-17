package utils;


import javax.swing.JFrame;
import org.ejml.simple.SimpleMatrix;
import common.HeatMapPanel;

import space.DiagonalCompositionSemanticSpace;

public class DiagonalCompositionHeatmap {
    public static void main(String[] args) {
        String compSpaceFile = "/home/thenghiapham/work/project/mikolov/output/wbnc.cmp";
//        String construction = "VP VB NP";
        String construction = "NP NN NN";
//        String construction = "NP JJ NN";
//        String construction = "S NP VP";
        DiagonalCompositionSemanticSpace space = DiagonalCompositionSemanticSpace.loadCompositionSpace(compSpaceFile, true);
        SimpleMatrix vector = space.getConstructionMatrix(construction);
        SimpleMatrix diaMatrix = SimpleMatrix.diag(vector.getMatrix().data);
        System.out.println(diaMatrix.extractMatrix(0, 10, 0, 10));
        int numCols = diaMatrix.numCols();
        int halfCols = numCols / 2;
        System.out.println(diaMatrix.extractMatrix(halfCols, halfCols + 10, halfCols, halfCols + 10));
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new HeatMapPanel(diaMatrix));
        f.setSize(diaMatrix.numCols() * 12, diaMatrix.numRows() * 12);
        f.setLocation(200,200);
        f.setVisible(true);
        
    }
}
