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

/**
 * This class takes care of the matrices for all the constructions
 * Each construction (eg. NP JJ NN) belong to a group of constructions.
 * Each matrix corresponds to one group of constructions, and is used in 
 * composing phrases' vectors that belong to that group.
 * Any construction that does not belong to any group is mapped to the
 * "default" group. The matrix for the default group can:
 * - either be fixed (identity)
 * - or learnt
 * @author pham
 *
 */
public class CompositionMatrices {
    public static final String DEFAULT_STRING = "default";
    public static final double DEFAULT_WEIGHT_DECAY = 1e-3;
    public static final double MAX_NORM = 10;
    // TODO: turn it back
//    public static final double DEFAULT_WEIGHT_DECAY = 0;
    protected SimpleMatrix[] compositionMatrices;

    // key for synchorizing when updating a specific matrix
    protected Integer[] keys;
    
    // map from contruction to construction's group id
    protected HashMap<String, String> constructionMap;
    
    // map from construction group id to matrix id
    // default matrix is at first position
    protected HashMap<String, Integer> groupMap;
    
    // weight decay for learning
    protected double weightDecay = DEFAULT_WEIGHT_DECAY;
    
    /****GROUP OF CONSTRUCTORS AND INITIALIZATION METHOD*/
    
    /**
     * protected constructor: cannot be called outside of the class (or subclass)
     * @param groupMap
     * @param constructionMap
     * @param compositionMatrices
     */
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
    
    /**
     * create an instance of CompositionMatrices with randomly initialization 
     * for all the matrices for all the constructions
     * The index of the matrix of the group starts at 1, since 0 is where
     * the matrix of the "default" group is
     * @param constructionMap: a map that maps each construction into a group
     * @param hiddenLayerSize: the size of the vectors
     * @return an instance CompositionMatrices
     */
    public static CompositionMatrices randomInitialize(HashMap<String, String> constructionMap, 
            int hiddenLayerSize) {
        // create the set of groups
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
        
        // create the array of matrices and a map to map from group to matrix index
        List<String> constructions = new ArrayList<String>(groups); 
        SimpleMatrix[] compositionMatrices = new SimpleMatrix[constructions.size() + 1];
        HashMap<String, Integer> groupMap= new HashMap<>();
        
        // initialize the matrices & fill in the map
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
    
    /**
     * create an instance of CompositionMatrices with identity-initialization 
     * for all the matrices for all the constructions
     * The index of the matrix of the group starts at 1, since 0 is where
     * the matrix of the "default" group is
     * @param constructionMap: a map that maps each construction into a group
     * @param hiddenLayerSize: the size of the vectors
     * @return an instance CompositionMatrices
     */
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
    
    /**
     * DEBUGGING PURPOSE
     * TODO: remove
     */
    public static CompositionMatrices createSimple( 
            SimpleMatrix defaultMatrix) {
        HashMap<String, String> constructionMap = new HashMap<String, String>();
        HashMap<String, Integer> groupMap= new HashMap<>();
        
        SimpleMatrix[] compositionMatrices = new SimpleMatrix[1];
        compositionMatrices[0] = defaultMatrix;
        groupMap.put(DEFAULT_STRING, 0);
        return new CompositionMatrices(groupMap, constructionMap, compositionMatrices);
    }
    
    
    /**
     * create a random n x 2n matrix
     * the values in the matrix are uniformly distributed in the range [-0.5/n, 0.5/n]
     * @param hiddenLayerSize: n
     * @return
     */
    protected static SimpleMatrix createRandomMatrix(int hiddenLayerSize) {
        Random random = new Random();
        SimpleMatrix randomMatrix1 = SimpleMatrix.random(hiddenLayerSize, hiddenLayerSize, - 0.5, 0.5, random);
        SimpleMatrix randomMatrix2 = SimpleMatrix.random(hiddenLayerSize, hiddenLayerSize, - 0.5, 0.5, random);
        SimpleMatrix result = SimpleMatrixUtils.hStack(randomMatrix1, randomMatrix2).scale(1 /(double) hiddenLayerSize);
//        System.out.println(result);
        return result;
    }
    
    /**
     * create a n x 2n matrix where its two halves are identity matrices
     * @param hiddenLayerSize
     * @return
     */
    public static SimpleMatrix createIdentityMatrix(int hiddenLayerSize) {
        SimpleMatrix identity = SimpleMatrix.identity(hiddenLayerSize);
        return SimpleMatrixUtils.hStack(identity, identity);
    }
    
    
    /**GROUP OF METHODS TO RETRIEVE THE MATRIX FOR THE CONSTRUCTIONS****/
    
    /**
     * retrieve the composition matrix from the index
     * @param constructionIndex
     * @return
     */
    public SimpleMatrix getCompositionMatrix(int constructionIndex) {
        return compositionMatrices[constructionIndex];
    }
    
    
    /**
     * Retrieve the index of the construction group for a construction
     * @param construction
     * @return
     */
    public int getConstructionIndex(String construction) {
        if (constructionMap.containsKey(construction)) {
            return groupMap.get(constructionMap.get(construction));
        } else {
            // unknown construction -> default group
            return groupMap.get(DEFAULT_STRING);
        }
    }
    
    /**
     * Retrieve the composition matrix for a construction
     * (It is the combination of the above two methods)
     * @param construction
     * @return
     */
    public SimpleMatrix getCompositionMatrix(String construction) {
        if (constructionMap.containsKey(construction)) {
            return compositionMatrices[groupMap.get(constructionMap.get(construction))];
        } else {
            // unknown construction -> default group
            return compositionMatrices[groupMap.get(DEFAULT_STRING)];
        }
    }
    
    
    /**GROUP OF UPDATING METHODS***/
    
    /**
     * synchorized-ish method
     * @param index
     * @param delta
     */
    protected void updateConstructionGroup(int index, SimpleMatrix delta) {
        // TODO: review this
        SimpleMatrix newMatrix = compositionMatrices[index].minus(delta);
        newMatrix = SimpleMatrixUtils.normalize(newMatrix, MAX_NORM);

        compositionMatrices[index] = newMatrix;
    }
    
    /**
     * Update the matrix of a construction group
     * @param index: the index of the matrix
     * @param gradient
     * @param learningRate
     */
    protected void updateConstructionGroup(int index, SimpleMatrix gradient, double learningRate) {
        updateConstructionGroup(index, gradient.scale(learningRate));
    }
    
    /**SAVE LOAD METHODS**/
    /**
     * 
     * @param constructionIndices
     * @param gradients
     * @param learningRate
     */
    public void updateMatrices(ArrayList<Integer> constructionIndices, ArrayList<SimpleMatrix> gradients, double learningRate) {
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

            // TODO: change it back
            // don't update default matrices
            if (key == 0) continue;
            
            SimpleMatrix gradient =  gradientMap.get(key);
            if (learningRate != 0) {
                gradient = gradient.plus(compositionMatrices[key].scale(weightDecayTimes.get(key) * weightDecay));
//                System.out.println("blah blah: " + weightDecay);
            }
            updateConstructionGroup(key, gradient.scale(learningRate));
        }
    }
    
    /**
     * Saving all the info & data  into a stream in 2 steps
     * - save the constructions + construction groups
     * - save the matrix with group's name as id
     * @param outputStream
     * @param binary
     * @throws IOException
     */
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
    
    /**
     * Load all the info & data from a stream in 2 steps
     * - load the constructions + construction groups
     * - load the matrix with group's name as id
     * @param outputStream
     * @param binary
     * @throws IOException
     */
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
    
    /**GET SET METHODS**/
    public void setWeightDecay(double weightDecay) {
        this.weightDecay = weightDecay;
    }
}
