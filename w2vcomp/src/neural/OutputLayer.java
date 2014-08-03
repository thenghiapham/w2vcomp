package neural;

import org.ejml.simple.SimpleMatrix;

public abstract class OutputLayer {
    protected HiddenLayer layer;
    protected CostFunction costFunction;
}
