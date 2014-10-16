package neural.function;

import org.ejml.simple.SimpleMatrix;

public class HierarchicalSoftmaxObjective2 implements ObjectiveFunction{
    // Since this is cost function, the value here is the opposite of 
    // the value in Mikolov's paper
    protected static final double SHIFT_VALUE = 0.005;
    protected static final double MULT = 1 - SHIFT_VALUE;
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
            if (goldValues[i] == 0) {
                cost += Math.log(SHIFT_VALUE + MULT * (predictedValues[i]));
            } else {
                cost += Math.log(SHIFT_VALUE + MULT * (1 - predictedValues[i]));
            }
        }
        return -cost;
    }

    /**
     * goldMatrix: the (Huffman) binary code of a word
     * predictedMatrix: outputs of the sigmoid function of the path from the
     *    root to the word in the hierarchy
     * return: the derivate of the objective function, which is
     *                 {0 if the sigmoid value is 0 or 1
     *                 {-1 / (sigmoid value) if gold code bit is 0 
     *                 {1 / (1 - sigmoid value) if gold code bit is 1
     */
    @Override
    public SimpleMatrix derivative(SimpleMatrix predictedMatrix, SimpleMatrix
            goldMatrix) {
        double[] predictedValues = predictedMatrix.getMatrix().getData();
        double[] goldValues = goldMatrix.getMatrix().getData();
        double[] rawError = new double[predictedValues.length];
        for (int i = 0; i < predictedValues.length; i++) {
            if (goldValues[i] == 0) {
                rawError[i] = -1 / (SHIFT_VALUE + MULT * (predictedValues[i]));
            } else {
                rawError[i] = 1 / (SHIFT_VALUE + MULT * (1 - predictedValues[i]));
            }
        }
        return new SimpleMatrix(predictedMatrix.numRows(), 
                predictedMatrix.numCols(), true, rawError);
    }

}
