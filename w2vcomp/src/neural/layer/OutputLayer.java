package neural.layer;

import neural.function.ActivationFunction;
import neural.function.ObjectiveFunction;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;
import common.exception.IllegalOperationException;

/**
 * This class represents an output layer (a network can have many output layers)
 * It cannot have out-coming layers
 * The in-coming layers can be
 *   - projection layer (for predicting target words of a single word)
 *   - hidden layer (for predicting target words of a phrase)
 * It has both an activation function and an objective function
 * 
 * @author thenghiapham
 *
 */
public class OutputLayer extends BasicLayer implements Layer{
    protected ObjectiveFunction costFunction;
    
    protected SimpleMatrix inputWeights;
    protected ActivationFunction activation;
    
    // Temporary computing result
    protected SimpleMatrix tempZ;
    
    // Combined input column vector
    protected SimpleMatrix input;
    
    // output vector
    protected SimpleMatrix output;
    
    protected SimpleMatrix error;
    
    protected SimpleMatrix gradient;
    
    protected SimpleMatrix goldOutput;
    
    // output of the cost function
    protected double cost;
    
    protected int[] weightVectorIndices;
    
    public OutputLayer(SimpleMatrix weights, ActivationFunction activation, SimpleMatrix goldOutput, ObjectiveFunction costFunction) {
        this.inputWeights = weights;
        this.activation = activation;
        this.goldOutput = goldOutput;
        this.costFunction = costFunction;
    }
    
    @Override
    public void forward() {
        // Exactly like in hidden layer
        input = getInLayerIntput();
        tempZ = inputWeights.mult(input);
        if (activation != null) 
            output = SimpleMatrixUtils.applyActivationFunction(tempZ,activation);
        else
            output = tempZ;
    }
    
    @Override
    public void addOutLayer(Layer outLayer) {
        throw new IllegalOperationException("Cannot add outLayer to an OutputLayer");
    }
    
    @Override
    public void backward() {
        // Similar to hidden layer's backward
        // the difference is that here the error coming directly from the
        // objective function
        SimpleMatrix outError = costFunction.derivative(output, goldOutput);
        if (activation != null) {
            outError = outError.elementMult(SimpleMatrixUtils.applyDerivative(tempZ, activation));
        }
        gradient = outError.mult(input.transpose());
        error = inputWeights.transpose().mult(outError);
    }

    /**
     * For debugging purpose
     * It is used in checking gradient
     * (Checking the gradient of the network involved sum up the cost
     * function of all the output layers) 
     * 
     * Can only be-called after calling forward
     * @return
     */
    public double getCost() {
        return costFunction.computeObjective(output, goldOutput);
    }
    
    @Override
    public SimpleMatrix getOutput() {
        // TODO Auto-generated method stub
        return output;
    }

    @Override
    public SimpleMatrix getError(Layer child) {
        // TODO Auto-generated method stub
        return error;
    }
    
    @Override
    public SimpleMatrix getGradient() {
        // TODO Auto-generated method stub
        return gradient;
    }
    
    public int[] getWeightVectorIndices() {
        return weightVectorIndices;
    }
    
    @Override
    public String getTypeString() {
        return "O";
    }
}
