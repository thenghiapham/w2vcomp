package neural;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

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
    
    protected SimpleMatrix getInLayerIntput() {
        ArrayList<SimpleMatrix> inputs = new ArrayList<>();
        for (Layer inLayer: inLayers) {
            inputs.add(inLayer.getOutput());
        }
//        System.out.println(toTreeString());
        return SimpleMatrixUtils.concatenateVectors(inputs);
    }
    
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
    
    public String toString() {
        return "B";
    }
    public String toTreeString() {
        String treeString = "("+ this.toString();
        if (inLayers.size() > 0) {
            treeString += " ";
            for (Layer child : inLayers)
                treeString += ((BasicLayer) child).toTreeString();
        }
        treeString += ")";
        return treeString;
    }


}
