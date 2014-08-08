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
    
    
    public HiddenLayer(SimpleMatrix weights, ActivationFunction activation) {
        this.inputWeights = weights;
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
        if (parentError == null) return;
        if (activation != null) {
            parentError = parentError.elementMult(SimpleMatrixUtils.applyDerivative(tempZ, activation));
        }
        gradient = parentError.mult(input.transpose());
        error = inputWeights.transpose().mult(parentError);
    }
    


    public SimpleMatrix getInput() {
        return input;
    }
    
    @Override
    public SimpleMatrix getError(Layer child) {
        // TODO: not that great
        if (error == null) return null;
        if (inLayers.size() == 0) return null;
        if (child == inLayers.get(0)) return SimpleMatrixUtils.extractPartialVector(error, 2, 0);
        else if (child == inLayers.get(1)) return SimpleMatrixUtils.extractPartialVector(error, 2, 1);
        else return null;
    }
    
    public SimpleMatrix getGradient() {
        return gradient;
    }

    @Override
    public SimpleMatrix getOutput() {
        return output;
    }
    
    public String toString() {
        return "H";
    }
}
