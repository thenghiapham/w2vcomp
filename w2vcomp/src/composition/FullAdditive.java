package composition;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

public class FullAdditive extends BasicComposition{
    protected SimpleMatrix compositionMatrix;
    public FullAdditive(SimpleMatrix compositionMatrix) {
        this.compositionMatrix = compositionMatrix;
    }
    
    // Everything is row vectors or row major matrices
    public SimpleMatrix compose(SimpleMatrix uMatrix, SimpleMatrix vMatrix) {
        return compositionMatrix.mult(SimpleMatrixUtils.vStack(uMatrix.transpose(), vMatrix.transpose())).transpose();
    }
    
}
