package neural;

import org.ejml.simple.SimpleMatrix;

public interface CostFunction {
    public SimpleMatrix cost(SimpleMatrix predicted, SimpleMatrix gold);
    public SimpleMatrix getError();
}
