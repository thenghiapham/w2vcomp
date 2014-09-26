package composition;

import neural.ActivationFunction;

import org.ejml.simple.SimpleMatrix;

import space.SemanticSpace;

import common.SimpleMatrixUtils;
import common.exception.UnimplementedException;

public class FullAdditive extends BasicComposition{
    protected SimpleMatrix compositionMatrix;
    protected ActivationFunction function;
    public FullAdditive(SimpleMatrix compositionMatrix) {
        this.compositionMatrix = compositionMatrix;
    }
    
    public FullAdditive(SimpleMatrix compositionMatrix, ActivationFunction function) {
        this.compositionMatrix = compositionMatrix;
        this.function = function;
    } 
    
    // Everything is row vectors or row major matrices
    public SimpleMatrix compose(SimpleMatrix uMatrix, SimpleMatrix vMatrix) {
        SimpleMatrix result = compositionMatrix.mult(SimpleMatrixUtils.vStack(uMatrix.transpose(), vMatrix.transpose())).transpose();
        if (function != null) {
            result = SimpleMatrixUtils.applyActivationFunction(result, function);
        }
        return result;
    }
    
    @Override
    public SemanticSpace composeSpace(SemanticSpace inputSpace, String[] phrases) {
        throw new UnimplementedException("this method is not applicable to FullAdditive");
    }
    
}
