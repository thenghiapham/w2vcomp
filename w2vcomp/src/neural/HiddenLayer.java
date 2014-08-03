package neural;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

public class HiddenLayer {
    protected SimpleMatrix inputWeights;
    protected ActivationFunction activation;
    protected SimpleMatrix tempZ;
    protected SimpleMatrix tempInput; 
    
    public HiddenLayer(SimpleMatrix inputWeight, ActivationFunction activation) {
        this.inputWeights = inputWeight;
        this.activation = activation;
    }
    
    public SimpleMatrix forward(SimpleMatrix input) {
        tempInput = input;
        tempZ = inputWeights.mult(input);
        if (activation != null) 
            return SimpleMatrixUtils.applyActivationFunction(tempZ,activation);
        else
            return tempZ;
    }
    
    public SimpleMatrix[] backward(SimpleMatrix error) {
        if (activation != null) {
            error = error.elementMult(SimpleMatrixUtils.applyDerivative(tempZ, activation));
        }
        SimpleMatrix[] errorAndGrad = new SimpleMatrix[2];
        SimpleMatrix gradient = error.mult(tempInput.transpose());
        SimpleMatrix backwardError = inputWeights.transpose().mult(error);
        errorAndGrad[0] = backwardError;
        errorAndGrad[1] = gradient;
        return errorAndGrad;
                
    }
}
