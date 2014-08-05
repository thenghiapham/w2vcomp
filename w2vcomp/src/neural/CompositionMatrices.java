package neural;

import java.util.ArrayList;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

public class CompositionMatrices {
    protected SimpleMatrix[] compositionMatrices;
    protected Integer[] keys;
    protected HashMap<String, Integer> constructionMap;
    
    public SimpleMatrix getCompositionMatrix(int constructionIndex) {
        return compositionMatrices[constructionIndex];
    }
    
    public int getConstructionIndex(String construction) {
        return constructionMap.get(construction);
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
        int gradientNum = gradients.size();
        for (int i = 0; i < gradientNum; i++) {
            int constructionIndex = constructionIndices.get(i);
            SimpleMatrix gradient = gradients.get(i);
            if (gradientMap.containsKey(constructionIndex)) {
                gradientMap.put(constructionIndex, gradientMap.get(constructionIndex).plus(gradient));
            } else {
                gradientMap.put(constructionIndex, gradient);
            }
        }
        for (Integer key : gradientMap.keySet()) {
            updateSingleConstruction(key, gradientMap.get(key).scale(learningRate));
        }
    }
}
