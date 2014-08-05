package neural;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

public abstract class LearningStrategy {
    protected SimpleMatrix outputVectors;
    public abstract int[] getOutputIndices(String word);
    public abstract SimpleMatrix getGoldOutput(String word);
    
    public LearningStrategy(SimpleMatrix outputVectors) {
        this.outputVectors = outputVectors;
    }
    
    public SimpleMatrix getOutputWeights(int[] indices) {
        return SimpleMatrixUtils.getRows(outputVectors, indices);
    }
    
    public void updateMatrix(int[] indices, SimpleMatrix gradients, double learningRate) {
        gradients = gradients.scale(learningRate);
        SimpleMatrix orginalRows = SimpleMatrixUtils.getRows(outputVectors, indices);
        SimpleMatrix newRows = orginalRows.plus(gradients);
        for (int i = 0; i < indices.length; i++) {
            outputVectors.setRow(indices[i], 0, newRows.extractVector(true, i).getMatrix().data);
        }
    }
    
}
