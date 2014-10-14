package neural.layer;


import org.ejml.simple.SimpleMatrix;

import common.exception.IllegalOperationException;

/**
 * Projection layer
 *    - the output of this layer is identical to the input
 *    - the gradient of this layer is identical to the error back-propagated 
 *      from the out-coming layers
 *    - cannot have in-coming layers
 * @author thenghiapham
 *
 */
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
    
    @Override
    public String getTypeString() {
        return "P";
    }

    @Override
    public SimpleMatrix getWeights() {
        // TODO Auto-generated method stub
        return vector;
    }

    @Override
    public void setWeights(SimpleMatrix weights) {
        // TODO Auto-generated method stub
        this.vector = weights;
    }
}
