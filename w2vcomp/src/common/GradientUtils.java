package common;

import org.ejml.simple.SimpleMatrix;

/**
 * This class provides utility methods for gradient descend approach
 * @author thenghiapham
 *
 */
public class GradientUtils {
    public static void updateWeights(double[][] weightMatrix, int[] indices, SimpleMatrix grads) {
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            for (int j = 0; j < grads.numCols(); j++) {
                weightMatrix[index][j] += grads.get(i, j);
            }
        }
    }
}
