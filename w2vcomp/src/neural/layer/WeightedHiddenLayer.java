package neural.layer;

import java.util.ArrayList;

import neural.function.ActivationFunction;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

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
public class WeightedHiddenLayer extends BasicLayer implements Layer{
    
    protected double alpha = 1;
    protected double beta = 1;
    protected ActivationFunction activation;
    
    protected ArrayList<SimpleMatrix> input;
    protected SimpleMatrix inputWeights;
    protected SimpleMatrix tempZ;
    protected SimpleMatrix output;
    protected SimpleMatrix error;
    protected SimpleMatrix gradient;
    
    
    public WeightedHiddenLayer(SimpleMatrix weights, ActivationFunction activation) {
        this.inputWeights = weights;
        alpha = weights.get(0);
        beta = weights.get(1);
        this.activation = activation;
    }
    
    @Override
    public void forward() {
        /*
         * - combine the input from the in-coming layers
         * - multiply with weight matrix with the input vector (z_i)
         * - apply activation function if exist (a_i)
         */
        input = getInLayerInputs();
//        SimpleMatrixUtils.checkNaN(input.get(0));
//        SimpleMatrixUtils.checkNaN(input.get(1));
        tempZ = input.get(0).scale(alpha).plus(input.get(1).scale(beta));
//        try {
//            SimpleMatrixUtils.checkNaN(tempZ);
//        } catch (ValueException e) {
//            e.printStackTrace();
//        }
        if (activation != null) 
            output = SimpleMatrixUtils.applyActivationFunction(tempZ,activation);
        else
            output = tempZ;
//        SimpleMatrixUtils.checkNaN(output);
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
//            SimpleMatrixUtils.checkNaN(parentError);
        }
        double[] rawGrad = new double[2]; 
        rawGrad[0] = parentError.dot(input.get(0));
        rawGrad[1] = parentError.dot(input.get(1));
        gradient = new SimpleMatrix(2, 1, true, rawGrad);
//        SimpleMatrixUtils.checkNaN(gradient);
        ArrayList<SimpleMatrix> childError = new ArrayList<>();
        childError.add(parentError.scale(alpha));
        childError.add(parentError.scale(beta));
        error = SimpleMatrixUtils.concatenateVectors(childError);
//        SimpleMatrixUtils.checkNaN(error);
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
