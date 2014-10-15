package neural.layer;

import neural.function.ActivationFunction;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;
import common.exception.ValueException;

/**
 * HiddenLayer:
 * - in-coming layers can be
 *     + projection layer
 *     + hidden layer
 * + out-coming layer can be
 *     + hidden layer
 *     + output layer
 * @author thenghiapham
 *
 */
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
        /*
         * - combine the input from the in-coming layers
         * - multiply with weight matrix with the input vector (z_i)
         * - apply activation function if exist (a_i)
         */
        input = getInLayerIntput();
        SimpleMatrixUtils.checkNaN(input);
        SimpleMatrixUtils.checkNaN(inputWeights);
        tempZ = inputWeights.mult(input);
        try {
            SimpleMatrixUtils.checkNaN(tempZ);
        } catch (ValueException e) {
            System.out.println("input " + input);
            System.out.println("inputWeights " + inputWeights);
        }
        SimpleMatrixUtils.checkNaN(tempZ);
        if (activation != null) 
            output = SimpleMatrixUtils.applyActivationFunction(tempZ,activation);
        else
            output = tempZ;
        SimpleMatrixUtils.checkNaN(output);
    }
    
    @Override
    public void backward() {
        /*
         * typical backward formula
         * - computing both gradient of the weights and backward error for the
         *   incoming layers
         */
        
        SimpleMatrix parentError = getOutLayerError();
        if (parentError == null) return;
        if (activation != null) {
            parentError = parentError.elementMult(SimpleMatrixUtils.applyDerivative(tempZ, activation));
            SimpleMatrixUtils.checkNaN(parentError);
        }
        gradient = parentError.mult(input.transpose());
        SimpleMatrixUtils.checkNaN(gradient);
        error = inputWeights.transpose().mult(parentError);
        SimpleMatrixUtils.checkNaN(error);
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
    
    @Override
    public SimpleMatrix getGradient() {
        return gradient;
    }

    @Override
    public SimpleMatrix getOutput() {
        return output;
    }
    
    @Override
    public String getTypeString() {
        return "H";
    }

    @Override
    public SimpleMatrix getWeights() {
        // TODO Auto-generated method stub
        return inputWeights;
    }

    @Override
    public void setWeights(SimpleMatrix weights) {
        // TODO Auto-generated method stub
        inputWeights = weights;
    }
}
