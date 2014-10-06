package neural;

import org.ejml.simple.SimpleMatrix;

public interface ObjectiveFunction {
    /**
     * compute the objective function given the output of the output layer and
     * the gold standard value
     * @param predicted: output of the output layer
     * @param gold: gold standard values
     * @return objective: the output of the objective function
     */
    public double computeObjective(SimpleMatrix predicted, SimpleMatrix gold);
    
    /**
     * compute the derivative of the objective function with respect to each of
     * the elements of the output matrix/vector of the output layer
     * @param predicted: output of the output layer
     * @param gold: gold standard values
     * @return derivative: a matrix/vector, which has the same size of predicted, where
     * every element is the derivate of the objective function with respect to the 
     * corresponding element in the output matrix/vector
     */
    public SimpleMatrix derivative(SimpleMatrix predicted, SimpleMatrix gold);
}
