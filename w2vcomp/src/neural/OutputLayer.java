package neural;

import org.ejml.simple.SimpleMatrix;

public abstract class OutputLayer {
    protected HiddenLayer layer;
    protected CostFunction costFunction;
    protected SimpleMatrix tmpPredicted;
    
    public double forwardCost(SimpleMatrix input, SimpleMatrix goldOutput) {
        tmpPredicted = layer.forward(input);
        return costFunction.computeCost(tmpPredicted, goldOutput);
    }
    
    public SimpleMatrix[] backward(SimpleMatrix goldOutput) {
        SimpleMatrix error = costFunction.getError(tmpPredicted, goldOutput);
        return layer.backward(error);
    }
}
