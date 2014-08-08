package neural;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

public class CompositionMatrices {
    protected SimpleMatrix[] compositionMatrices;
    protected Integer[] keys;
    protected HashMap<String, Integer> constructionMap;
    protected double weightDecay = 1e-4;
    
    protected CompositionMatrices(HashMap<String, Integer> constructionMap, SimpleMatrix[] compositionMatrices) {
        this.compositionMatrices = compositionMatrices;
        this.constructionMap = constructionMap;
        this.keys = new Integer[compositionMatrices.length];
        for (int i = 0; i < compositionMatrices.length; i++) {
            keys[i] = i;
        }
    }
    
    public static CompositionMatrices randomInitialize(List<String> constructions, int hiddenLayerSize) {
        HashMap<String, Integer> constructionMap= new HashMap<>();
        SimpleMatrix[] compositionMatrices = new SimpleMatrix[constructions.size() + 1];
        int index = 0;
        compositionMatrices[index] = createRandomMatrix(hiddenLayerSize);
        for (String construction : constructions) {
            index++;
            constructionMap.put(construction, index);
            compositionMatrices[index] = createRandomMatrix(hiddenLayerSize);
        }
        return new CompositionMatrices(constructionMap, compositionMatrices);
    }
    
    public static CompositionMatrices identityInitialize(List<String> constructions, int hiddenLayerSize) {
        HashMap<String, Integer> constructionMap= new HashMap<>();
        SimpleMatrix[] compositionMatrices = new SimpleMatrix[constructions.size() + 1];
        int index = 0;
        compositionMatrices[index] = createIdentityMatrix(hiddenLayerSize);
        for (String construction : constructions) {
            index++;
            constructionMap.put(construction, index);
            compositionMatrices[index] = createIdentityMatrix(hiddenLayerSize);
        }
        return new CompositionMatrices(constructionMap, compositionMatrices);
    }
    
    protected static SimpleMatrix createRandomMatrix(int hiddenLayerSize) {
        Random random = new Random();
        SimpleMatrix randomMatrix1 = SimpleMatrix.random(hiddenLayerSize, hiddenLayerSize, - 0.5, 0.5, random);
        SimpleMatrix randomMatrix2 = SimpleMatrix.random(hiddenLayerSize, hiddenLayerSize, - 0.5, 0.5, random);
        return SimpleMatrixUtils.hStack(randomMatrix1, randomMatrix2).scale(1 / hiddenLayerSize);
    }
    
    public static SimpleMatrix createIdentityMatrix(int hiddenLayerSize) {
        SimpleMatrix identity = SimpleMatrix.identity(hiddenLayerSize);
        return SimpleMatrixUtils.hStack(identity, identity);
    }
    
    public SimpleMatrix getCompositionMatrix(int constructionIndex) {
        return compositionMatrices[constructionIndex];
    }
    
    public int getConstructionIndex(String construction) {
        if (constructionMap.containsKey(construction))
            return constructionMap.get(construction);
        else
            return 0;
    }
    
    public SimpleMatrix getCompositionMatrix(String construction) {
        if (constructionMap.containsKey(construction)) {
            return compositionMatrices[constructionMap.get(construction)];
        } else
            // default construction
            return compositionMatrices[0];
    }
    
    protected void updateSingleConstruction(int index, SimpleMatrix gradient) {
        // TODO: review this
        synchronized (keys[index]) {
            compositionMatrices[index] = compositionMatrices[index].plus(gradient);
        }
    }
    
    public void updateConstruction(int index, SimpleMatrix gradient, double learningRate) {
        updateSingleConstruction(index, gradient.scale(learningRate));
    }
    
    public void updateConstructions(ArrayList<Integer> constructionIndices, ArrayList<SimpleMatrix> gradients, double learningRate) {
        HashMap<Integer, SimpleMatrix> gradientMap = new HashMap<>();
        HashMap<Integer, Integer> weightDecayTimes = new HashMap<>();
        int gradientNum = gradients.size();
        for (int i = 0; i < gradientNum; i++) {
            int constructionIndex = constructionIndices.get(i);
            SimpleMatrix gradient = gradients.get(i);
            if (gradient == null) continue;
            if (gradientMap.containsKey(constructionIndex)) {
                gradientMap.put(constructionIndex, gradientMap.get(constructionIndex).plus(gradient));
                weightDecayTimes.put(constructionIndex, weightDecayTimes.get(constructionIndex) + 1);
            } else {
                gradientMap.put(constructionIndex, gradient);
                weightDecayTimes.put(constructionIndex, 1);
            }
        }
        for (Integer key : gradientMap.keySet()) {
            SimpleMatrix gradient =  gradientMap.get(key);
            if (learningRate != 0) {
                gradient = gradient.minus(compositionMatrices[key].scale(weightDecayTimes.get(key) * weightDecay));
            }
            updateSingleConstruction(key, gradient.scale(learningRate));
        }
    }
    
    public void setWeightDecay(double weightDecay) {
        this.weightDecay = weightDecay;
    }
}
