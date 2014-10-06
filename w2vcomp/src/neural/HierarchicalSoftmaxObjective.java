package neural;

import org.ejml.simple.SimpleMatrix;

public class HierarchicalSoftmaxObjective implements ObjectiveFunction{

    /**
     * goldMatrix: the (Huffman) binary code of a word
     * predictedMatrix: outputs of the sigmoid function of the path from the
     *    root to the word in the hierarchy
     * return: sum {if the sigmoid value is zero, don't take it into account
     *             {log(sigmoid value) if gold code bit is 0 
     *             {1 - log(sigmoid value) if gold code bit is 1
     *             
     */
    @Override
    public double computeObjective(SimpleMatrix predictedMatrix, SimpleMatrix 
            goldMatrix) {
        // TODO: check the special case 0, 1
        double[] predictedValues = predictedMatrix.getMatrix().getData();
        double[] goldValues = goldMatrix.getMatrix().getData();
        double cost = 0;
        for (int i = 0; i < predictedValues.length; i++) {
            if (predictedValues[i] == 0 || predictedValues[i] == 1) continue;
            else {
                if (goldValues[i] == 0) {
                    cost += Math.log(predictedValues[i]);
                } else {
                    cost += Math.log(1 - predictedValues[i]);
                }
            }
        }
        return cost;
    }

    /**
     * goldMatrix: the (Huffman) binary code of a word
     * predictedMatrix: outputs of the sigmoid function of the path from the
     *    root to the word in the hierarchy
     * return: the derivate of the objective function, which is
     *             sum {if the sigmoid value is zero, don't take it into account
     *                 {1 / (sigmoid value) if gold code bit is 0 
     *                 {-1 / (1 - sigmoid value) if gold code bit is 1
     */
    @Override
    public SimpleMatrix derivative(SimpleMatrix predictedMatrix, SimpleMatrix
            goldMatrix) {
        double[] predictedValues = predictedMatrix.getMatrix().getData();
        double[] goldValues = goldMatrix.getMatrix().getData();
        double[] rawError = new double[predictedValues.length];
        for (int i = 0; i < predictedValues.length; i++) {
            if (predictedValues[i] == 0 || predictedValues[i] == 1) {
                rawError[i] = 0;
            } else {
                if (goldValues[i] == 0) {
                    rawError[i] = 1 / predictedValues[i];
                } else {
                    rawError[i] = - 1 / (1 - predictedValues[i]);
                }
            }
        }
        return new SimpleMatrix(predictedMatrix.numRows(), 
                predictedMatrix.numCols(), true, rawError);
    }

}
