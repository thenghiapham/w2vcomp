package neural.layer;

import org.ejml.simple.SimpleMatrix;

/**
 * This interface represents a layer in the neural network.
 * Each layer has
 *     - some layers as input
 *     - some layers as output
 * @author thenghiapham
 *
 */
public interface Layer {
    /**
     * Forward step:
     *   - computing intermediate result for objective function
     * Should call this method on each layer in the hierarchical network bottom up
     */
    public void forward();
    
    /**
     * Backward step:
     *   - computing the derivative of the objective function given the input
     * Should call this method on each layer in the hierarchical network top down
     */
    public void backward();
    
    /**
     * add a layer to the list of in-coming layers
     * (for a hidden layer this list should contains 2, in case of learning from 
     * binary parse trees)
     * @param inLayer
     */
    public void addInLayer(Layer inLayer);
    
    /**
     * add a layer to the list of out-coming layers
     * (an out-coming layer can be a hidden layer or an output layer)
     * @param inLayer
     */
    public void addOutLayer(Layer outLayer);
    
    /**
     * get the output of the layer with respect to the input
     * @return
     */
    public SimpleMatrix getOutput();
    
    /**
     * return the back propagate error/derivative of the objective function with 
     * respect to the output of the specific incoming layer 
     * @param inLayer
     * @return
     */
    public SimpleMatrix getError(Layer inLayer);
    // TODO: change the name of this function since Error seems related to
    // cost function, not objective function
    
    /**
     * return the gradient of the weights of this layer
     * Since the weights of a layer are taken from either:
     *   - projection matrix
     *   - construction matrix
     *   - hierarchical softmax or negative sampling matrix
     * the layer should not update the weights itself
     * @return
     */
    public SimpleMatrix getGradient();
    
}
