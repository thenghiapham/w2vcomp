package neural;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;
import common.exception.IllegalOperationException;

public class OutputLayer extends BasicLayer implements Layer{
    protected CostFunction costFunction;
    
    protected SimpleMatrix inputWeights;
    protected ActivationFunction activation;
    
    
    protected SimpleMatrix tempZ;
    protected SimpleMatrix input;
    protected SimpleMatrix output;
    protected SimpleMatrix error;
    protected SimpleMatrix gradient;
    
    protected SimpleMatrix goldOutput;
    protected double cost;
    
    protected int[] weightVectorIndices;
    
    public OutputLayer(SimpleMatrix weights, ActivationFunction activation, SimpleMatrix goldOutput, CostFunction costFunction) {
        this.inputWeights = weights;
        this.activation = activation;
        this.goldOutput = goldOutput;
        this.costFunction = costFunction;
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
    public void addOutLayer(Layer outLayer) {
        throw new IllegalOperationException("Cannot add outLayer to an OutputLayer");
    }
    
    @Override
    public void backward() {
        SimpleMatrix outError = costFunction.getError(output, goldOutput);
        if (activation != null) {
            outError = outError.elementMult(SimpleMatrixUtils.applyDerivative(tempZ, activation));
        }
        gradient = error.mult(input.transpose());
        error = inputWeights.transpose().mult(outError);
    }

    public double getCost() {
        return cost;
    }
    
    @Override
    public SimpleMatrix getOutput() {
        // TODO Auto-generated method stub
        return output;
    }

    @Override
    public SimpleMatrix getError() {
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
}
