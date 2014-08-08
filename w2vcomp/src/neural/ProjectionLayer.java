package neural;

import org.ejml.simple.SimpleMatrix;

import common.exception.IllegalOperationException;


public class ProjectionLayer extends BasicLayer implements Layer{

    protected SimpleMatrix vector;
    protected SimpleMatrix error;
    protected SimpleMatrix gradient;
    
    public ProjectionLayer(SimpleMatrix vector) {
        this.vector = vector;
    }
    
    @Override
    public void forward() {
    }

    @Override
    public void backward() {
        error = getOutLayerError();
        gradient = error;
    }

    @Override
    public SimpleMatrix getOutput() {
        return vector;
    }

    @Override
    public SimpleMatrix getError(Layer child) {
        return error;
    }
    
    public SimpleMatrix getGradient() {
        return gradient;
    }
    
    @Override
    public void addInLayer(Layer inLayer) {
        throw new IllegalOperationException("Cannot add inLayer to a ProjectionLayer");
    }
    
    public String toString() {
        return "P";
    }
}
