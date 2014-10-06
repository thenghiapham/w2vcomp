package neural.function;

import org.ejml.simple.SimpleMatrix;

public class NegativeSamplingObjective implements ObjectiveFunction{
    // Since this is cost function, the value here is the opposite of 
    // the value in Mikolov's paper

    /**
     * goldMatrix: a one-hot array, where the first element is 1, indicating
     *             the target word
     * predictedMatrix: outputs of the sigmoid function with respect to
     *             the target word, & the negative samples
     * return: - sum {if the sigmoid value is 0 or 1, don't take it into account
     *             {log(sigmoid value) if the gold value is 1 
     *             {1 - log(sigmoid value) if gold code bit is 0
     *             
     */
    @Override
    public double computeObjective(SimpleMatrix predictedMatrix, SimpleMatrix 
            goldMatrix) {
        // TODO: also check the special cases: 0 and 1
        //       it seems to make a difference when the point is very close to 
        //       0 or 1 and got rounded by the pre-computed table
        double[] predictedValues = predictedMatrix.getMatrix().getData();
        double[] goldValues = goldMatrix.getMatrix().getData();
        double cost = 0;
        for (int i = 0; i < predictedValues.length; i++) {
            if (predictedValues[i] == 0 || predictedValues[i] == 1) continue;
            else {
                if (goldValues[i] == 1) {
                    cost += Math.log(predictedValues[i]);
                } else {
                    cost += Math.log(1 - predictedValues[i]);
                }
            }
        }
        return -cost;
    }

    /**
     * goldMatrix: a one-hot array, where the first element is 1, indicating
     *             the target word
     * predictedMatrix: outputs of the sigmoid function with respect to
     *             the target word, & the negative samples
     * return:     {0 if the sigmoid value is 0 or 1
     *             {-1 / (sigmoid value) if the gold value is 1 
     *             {1 / (1 - sigmoid value) if gold code bit is 0
     *             
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
                // TODO: problem with cutting off value of sigmoid here
                if (goldValues[i] == 1) {
                    rawError[i] = -1 / predictedValues[i];
                } else {
                    rawError[i] = 1 / (1 - predictedValues[i]);
                }
            }
        }
        return new SimpleMatrix(predictedMatrix.numRows(), 
                predictedMatrix.numCols(), true, rawError);
    }
    
}
