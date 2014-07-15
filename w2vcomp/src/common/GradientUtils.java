package common;

import org.ejml.simple.SimpleMatrix;

public class GradientUtils {
    // TODO: fill in this one
    public static void updateWeights(double[][] weightMatrix, int[] indices, SimpleMatrix grads) {
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            for (int j = 0; j < grads.numCols(); j++) {
                weightMatrix[index][j] += grads.get(i, j);
            }
        }
    }
}
