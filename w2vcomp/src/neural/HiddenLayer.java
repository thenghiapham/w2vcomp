package neural;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

public class HiddenLayer extends BasicLayer implements Layer{
    
    protected SimpleMatrix inputWeights;
    protected ActivationFunction activation;
    
    protected SimpleMatrix tempZ;
    protected SimpleMatrix input;
    protected SimpleMatrix output;
    protected SimpleMatrix error;
    protected SimpleMatrix gradient;
    
    
    public HiddenLayer(SimpleMatrix inputWeight, ActivationFunction activation) {
        this.inputWeights = inputWeight;
        this.activation = activation;
    }
    
    @Override
    public void forward() {
        input = getInLayerIntput();
        tempZ = inputWeights.mult(input);
        if (activation != null) 
            output = SimpleMatrixUtils.applyActivationFunction(tempZ,activation);
        else
            output = tempZ;
    }
    
    @Override
    public void backward() {
        SimpleMatrix parentError = getOutLayerError();
        if (activation != null) {
            parentError = parentError.elementMult(SimpleMatrixUtils.applyDerivative(tempZ, activation));
        }
        gradient = error.mult(input.transpose());
        error = inputWeights.transpose().mult(parentError);
    }
    


    public SimpleMatrix getInput() {
        return input;
    }
    
    @Override
    public SimpleMatrix getError() {
        return error;
    }
    
    public SimpleMatrix getGradient() {
        return gradient;
    }

    @Override
    public SimpleMatrix getOutput() {
        return output;
    }
}
