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
            inputs.add(inLayer.getError());
        }
        return SimpleMatrixUtils.concatenateVectors(inputs, true);
    }
    
    protected SimpleMatrix getOutLayerError() {
        ArrayList<SimpleMatrix> errors = new ArrayList<>();
        for (Layer outLayer: outLayers) {
            errors.add(outLayer.getError());
        }
        SimpleMatrix result = new SimpleMatrix(errors.get(0).numRows(),errors.get(0).numRows());
        for (SimpleMatrix error : errors)
            result = result.plus(error);
        return result;
    }


}
