package neural;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

public class CompositionMatrices {
    public static final String DEFAULT_STRING = "default";
    protected SimpleMatrix[] compositionMatrices;

    // key for synchorizing
    protected Integer[] keys;
    
    // map from contruction to construction's group id
    protected HashMap<String, String> constructionMap;
    
    // map from construction group id to matrix id
    // default matrix is at first position
    protected HashMap<String, Integer> groupMap;
    
    // weight decay for learning
    protected double weightDecay = 1e-4;
    
    protected CompositionMatrices(HashMap<String, Integer> groupMap, 
            HashMap<String, String> constructionMap, 
            SimpleMatrix[] compositionMatrices) {
        this.compositionMatrices = compositionMatrices;
        this.groupMap = groupMap;
        this.constructionMap = constructionMap;
        this.keys = new Integer[compositionMatrices.length];
        for (int i = 0; i < compositionMatrices.length; i++) {
            keys[i] = i;
        }
    }
    
    public static CompositionMatrices randomInitialize(HashMap<String, String> constructionMap, 
            int hiddenLayerSize) {
        HashSet<String> groups = new HashSet<String>();
        if (constructionMap != null) {
            for (String key: constructionMap.keySet()) {
                String group = constructionMap.get(key);
                if (group != null && !groups.contains(group)) {
                    groups.add(group);
                }
            }
        } else {
            constructionMap = new HashMap<String, String>();
        }
        
        
        List<String> constructions = new ArrayList<String>(groups); 
        HashMap<String, Integer> groupMap= new HashMap<>();
        SimpleMatrix[] compositionMatrices = new SimpleMatrix[constructions.size() + 1];
        int index = 0;
        compositionMatrices[index] = createRandomMatrix(hiddenLayerSize);
        groupMap.put(DEFAULT_STRING, 0);
        for (String construction : constructions) {
            index++;
            groupMap.put(construction, index);
            compositionMatrices[index] = createRandomMatrix(hiddenLayerSize);
        }
        return new CompositionMatrices(groupMap, constructionMap, compositionMatrices);
    }
    
    public static CompositionMatrices identityInitialize(HashMap<String, String> constructionMap, 
            int hiddenLayerSize) {
        HashSet<String> groups = new HashSet<String>();
        if (constructionMap != null) {
            for (String key: constructionMap.keySet()) {
                String group = constructionMap.get(key);
                if (group != null && !groups.contains(group)) {
                    groups.add(group);
                }
            }
        } else {
            constructionMap = new HashMap<String, String>();
        }
        
        List<String> constructions = new ArrayList<String>(groups); 
        
        HashMap<String, Integer> groupMap= new HashMap<>();
        SimpleMatrix[] compositionMatrices = new SimpleMatrix[constructions.size() + 1];
        int index = 0;
        compositionMatrices[index] = createIdentityMatrix(hiddenLayerSize);
        groupMap.put(DEFAULT_STRING, 0);
        
        for (String construction : constructions) {
            index++;
            groupMap.put(construction, index);
            compositionMatrices[index] = createIdentityMatrix(hiddenLayerSize);
        }
        return new CompositionMatrices(groupMap, constructionMap, compositionMatrices);
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
        if (constructionMap.containsKey(construction)) {
            return groupMap.get(constructionMap.get(construction));
        } else {
            // default
            return groupMap.get(DEFAULT_STRING);
        }
    }
    
    public SimpleMatrix getCompositionMatrix(String construction) {
        if (constructionMap.containsKey(construction)) {
            return compositionMatrices[groupMap.get(constructionMap.get(construction))];
        } else {
            // default construction
            return compositionMatrices[groupMap.get(DEFAULT_STRING)];
        }
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
    
    public void saveConstructionMatrices(BufferedOutputStream outputStream, boolean binary) throws IOException{
        outputStream.write(("" + constructionMap.keySet().size() + "\n").getBytes());
        for (String construction : constructionMap.keySet()) {
            outputStream.write((construction + " " + constructionMap.get(construction) + "\n").getBytes());
        }
        outputStream.write(("" + groupMap.keySet().size() + "\n").getBytes());
        for (String group : groupMap.keySet()) {
            outputStream.write((group + "\n").getBytes());
            IOUtils.saveMatrix(outputStream, compositionMatrices[groupMap.get(group)], binary);
        }
    }
    
    public static CompositionMatrices loadConstructionMatrices(BufferedInputStream inputStream, boolean binary) throws IOException{
        int constructionNumber = Integer.parseInt(IOUtils.readWord(inputStream));
        HashMap<String, String> constructionMap = new HashMap<>();
        for (int i = 0; i < constructionNumber; i++) {
            String root = IOUtils.readWord(inputStream);
            String child1 = IOUtils.readWord(inputStream);
            String child2 = IOUtils.readWord(inputStream);
            String group = IOUtils.readWord(inputStream);
            String construction = root + " " + child1 + " " + child2;
            constructionMap.put(construction, group);
        }
        
        int groupNumber = Integer.parseInt(IOUtils.readWord(inputStream));
        SimpleMatrix[] compositionMatrices = new SimpleMatrix[groupNumber];
        HashMap<String, Integer> groupMap = new HashMap<>();
        for (int i = 0; i < groupNumber; i++) {
            String group = IOUtils.readWord(inputStream);
            groupMap.put(group, i);
            compositionMatrices[i] = new SimpleMatrix(IOUtils.readMatrix(inputStream, binary));
        }
        return new CompositionMatrices(groupMap, constructionMap, compositionMatrices);
    }
}
