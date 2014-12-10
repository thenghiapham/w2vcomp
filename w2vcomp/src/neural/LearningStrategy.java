package neural;

import neural.function.ObjectiveFunction;

import org.ejml.simple.SimpleMatrix;

public abstract class LearningStrategy {
    protected double[][] outputVectors;
    protected int vectorSize;
    public abstract int[] getOutputIndices(String word);
    public abstract SimpleMatrix getGoldOutput(String word);
    public abstract ObjectiveFunction getCostFunction();
    
    public LearningStrategy(double[][] outputVectors) {
        this.outputVectors = outputVectors;
        vectorSize = outputVectors[0].length;
        System.out.println("vecSize:" + vectorSize);
    }
    
    public SimpleMatrix getOutputWeights(int[] indices) {
        // TODO: transpose all?
        double[][] weights = new double[indices.length][];
        for (int i = 0; i < indices.length; i++) {
            weights[i] = outputVectors[indices[i]];
        }
        return new SimpleMatrix(weights);
    }
    
    public void updateMatrix(int[] indices, SimpleMatrix gradients, double learningRate) {
//        gradients = gradients.scale(learningRate);
        
        double[] gradData = gradients.getMatrix().data;
        for (int i = 0; i < indices.length; i++) {
            int gradPos = i * vectorSize;
            for (int j = 0; j < vectorSize; j++)
                outputVectors[indices[i]][j] -= gradData[gradPos + j] * learningRate;
        }
        
    }
    
    //TODO: copy so that it wouldn't be modified?
    public double[][] getMatrix() {
        return outputVectors;
    }
    
}
