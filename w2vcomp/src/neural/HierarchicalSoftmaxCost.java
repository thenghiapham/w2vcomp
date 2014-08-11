package neural;

import org.ejml.simple.SimpleMatrix;

public class HierarchicalSoftmaxCost implements CostFunction{

    @Override
    public double computeCost(SimpleMatrix predictedMatrix, SimpleMatrix 
            goldMatrix) {
        // TODO Auto-generated method stub
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

    @Override
    public SimpleMatrix getError(SimpleMatrix predictedMatrix, SimpleMatrix
            goldMatrix) {
        // TODO Auto-generated method stub
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
