package composition;

import org.ejml.simple.SimpleMatrix;

public class WeightedAdditive extends BasicComposition{
    protected double alpha;
    protected double beta;
    
    public WeightedAdditive() {
        alpha = 1;
        beta = 1;
    }
    
    public WeightedAdditive(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }
    
 // Everything is row vectors or row major matrices
    public SimpleMatrix compose(SimpleMatrix uMatrix, SimpleMatrix vMatrix) {
        if (uMatrix == null) return vMatrix;
        if (vMatrix == null) return uMatrix;
        return uMatrix.scale(alpha).plus(vMatrix.scale(beta));
    }
    
}
