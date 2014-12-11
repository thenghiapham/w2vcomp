package neural.function;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;
import common.exception.UnimplementedException;

public class SoftmaxObjective implements ObjectiveFunction{

    @Override
    public double computeObjective(SimpleMatrix predicted, SimpleMatrix gold) {
        // TODO Auto-generated method stub
        throw new UnimplementedException("Not implemented");
    }

    @Override
    public SimpleMatrix derivative(SimpleMatrix predicted, SimpleMatrix gold) {
        // TODO Auto-generated method stub
        double max = SimpleMatrixUtils.elementMax(predicted);
        double[] data = predicted.getMatrix().data;
        double[] goldData = gold.getMatrix().data;
        double[] eData = new double[data.length];
        SimpleMatrix result = new SimpleMatrix(predicted.numRows(), predicted.numCols());
        double[] resultData = result.getMatrix().data;
        double sumE = 0;
        
        for (int i = 0; i < data.length; i++) {
            eData[i] = Math.exp(goldData[i]-max); 
            sumE += eData[i];
        }
        
        double hTarget = 0;
        for (int i = 0; i < data.length; i++) {
            eData[i] /= sumE;
            if (goldData[i] == 1) {
                hTarget = eData[i];
            }
        }
        
        for (int i = 0; i < data.length; i++) {
            resultData[i] = hTarget * (goldData[i] - eData[i]);
        }
        return result;
    }
}
