package neural;

import org.ejml.simple.SimpleMatrix;

public interface CostFunction {
    public double computeCost(SimpleMatrix predicted, SimpleMatrix gold);
    public SimpleMatrix getError(SimpleMatrix predictedMatrix, SimpleMatrix goldMatrix);
}
