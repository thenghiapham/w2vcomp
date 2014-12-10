package neural.layer;

import java.util.ArrayList;


import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

/**
 * This abstract class implements some of the basic method defined in Layer 
 * interface.
 * 
 * In this implementation, input vector for every layer is column vector
 * @author thenghiapham
 *
 */
public abstract class BasicLayer implements Layer {

    protected ArrayList<Layer> inLayers;
    protected ArrayList<Layer> outLayers;
    
    public BasicLayer() {
        inLayers = new ArrayList<>();
        outLayers = new ArrayList<>();
    }
    
    
    @Override
    public void addInLayer(Layer inLayer) {
        inLayers.add(inLayer);
    }

    @Override
    public void addOutLayer(Layer outLayer) {
        outLayers.add(outLayer);
    }
    
    /**
     * Concatenate the input matrices (from the incoming layer)
     * into the a single input matrix
     * @return
     */
    protected SimpleMatrix getInLayerInput() {
        ArrayList<SimpleMatrix> inputs = new ArrayList<>();
        for (Layer inLayer: inLayers) {
            inputs.add(inLayer.getOutput());
        }
        return SimpleMatrixUtils.concatenateVectors(inputs);
    }
    
    protected ArrayList<SimpleMatrix> getInLayerInputs() {
        ArrayList<SimpleMatrix> inputs = new ArrayList<>();
        for (Layer inLayer: inLayers) {
            inputs.add(inLayer.getOutput());
        }
        return inputs;
    }
    
    /**
     * Sum up all the back-propagate error from the out-coming layers
     * @return
     */
    protected SimpleMatrix getOutLayerError() {
        if (outLayers.size() == 0) return null;
        ArrayList<SimpleMatrix> errors = new ArrayList<>();
        for (Layer outLayer: outLayers) {
            SimpleMatrix outError = outLayer.getError(this);
            if (outError != null)
                errors.add(outError);
        }
        if (errors.size() == 0) return null;
        SimpleMatrix result = errors.get(0);
        for (int i = 1; i < errors.size(); i++) {
            result = result.plus(errors.get(i));
        }
        return result;
    }
    
    /**
     * For gradientChecking purpose
     * Return the string indicating the type of the layer
     */
    public abstract SimpleMatrix getWeights();
    public abstract void setWeights(SimpleMatrix weights);
    
    
    /**
     * For debuging purpose
     * Return the string indicating the type of the layer
     */
    public abstract String getTypeString();
    
    /**
     * For debuging purpose:
     * return a string indicating the structure of the layers below it in a tree
     */
    public String toTreeString() {
        String treeString = "("+ this.getTypeString();
        if (inLayers.size() > 0) {
            treeString += " ";
            for (Layer child : inLayers)
                treeString += ((BasicLayer) child).toTreeString();
        }
        treeString += ")";
        return treeString;
    }

    public int getOutSize() {
        return outLayers.size();
    }

}
