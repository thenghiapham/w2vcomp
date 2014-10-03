package neural;

import org.ejml.simple.SimpleMatrix;

public interface CostFunction {
    /**
     * compute the cost function given the output of the output layer and
     * the gold standard value
     * @param predicted: output of the output layer
     * @param gold: gold standard values
     * @return cost: the output of the cost function
     */
    public double computeCost(SimpleMatrix predicted, SimpleMatrix gold);
    
    /**
     * compute the derivative of the cost function with respect to each of
     * the elements of the output matrix/vector of the output layer
     * @param predicted: output of the output layer
     * @param gold: gold standard values
     * @return derivative: a matrix/vector, which has the same size of predicted, where
     * every element is the derivate of the cost function with respect to the 
     * corresponding element in the output matrix/vector
     */
    public SimpleMatrix derivative(SimpleMatrix predicted, SimpleMatrix gold);
}
