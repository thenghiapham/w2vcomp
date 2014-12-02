package neural;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

public class WeightedCompositionMatrices extends CompositionMatrices{
    protected Random rand = new Random();

    protected WeightedCompositionMatrices(HashMap<String, Integer> groupMap,
            HashMap<String, String> constructionMap,
            SimpleMatrix[] compositionMatrices) {
        super(groupMap, constructionMap, compositionMatrices);
        // TODO Auto-generated constructor stub
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
        public static WeightedCompositionMatrices randomInitialize(HashMap<String, String> constructionMap, 
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
            ArrayList<String> constructions = new ArrayList<String>(groups); 
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
            return new WeightedCompositionMatrices(groupMap, constructionMap, compositionMatrices);
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
        public static WeightedCompositionMatrices identityInitialize(HashMap<String, String> constructionMap, 
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
            
            ArrayList<String> constructions = new ArrayList<String>(groups); 
            
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
            return new WeightedCompositionMatrices(groupMap, constructionMap, compositionMatrices);
        }
        
        /**
         * DEBUGGING PURPOSE
         * TODO: remove
         */
        public static WeightedCompositionMatrices createSimple( 
                SimpleMatrix defaultMatrix) {
            HashMap<String, String> constructionMap = new HashMap<String, String>();
            HashMap<String, Integer> groupMap= new HashMap<>();
            
            SimpleMatrix[] compositionMatrices = new SimpleMatrix[1];
            compositionMatrices[0] = defaultMatrix;
            groupMap.put(DEFAULT_STRING, 0);
            return new WeightedCompositionMatrices(groupMap, constructionMap, compositionMatrices);
        }
        
        
        /**
         * create a random n x 2n matrix
         * the values in the matrix are uniformly distributed in the range [-0.5/n, 0.5/n]
         * @param hiddenLayerSize: n
         * @return
         */
        protected static SimpleMatrix createRandomMatrix(int hiddenLayerSize) {
            Random random = new Random();
            SimpleMatrix result = SimpleMatrix.random(2, 1, - 0.5, 0.5, random);
            return result;
        }
        
        /**
         * create a n x 2n matrix where its two halves are identity matrices
         * @param hiddenLayerSize
         * @return
         */
        public static SimpleMatrix createIdentityMatrix(int hiddenLayerSize) {
            SimpleMatrix result = SimpleMatrixUtils.createUniformMatrix(2, 1, 1);
            return result;
        }
    
        public static WeightedCompositionMatrices loadConstructionMatrices(BufferedInputStream inputStream, boolean binary) throws IOException{
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
            return new WeightedCompositionMatrices(groupMap, constructionMap, compositionMatrices);
        }
        
        protected void updateConstructionGroup(int index, SimpleMatrix delta) {
            // TODO: review this
            SimpleMatrix newMatrix = compositionMatrices[index].minus(delta);
//            newMatrix = SimpleMatrixUtils.normalize(newMatrix, MAX_NORM);
            // TODO: remove scale
            
            if (rand.nextFloat() > 1) {
//            if (rand.nextFloat() <= 0.1) {
                double sum = newMatrix.elementSum();
                newMatrix = newMatrix.scale(2 / sum);
            }
            compositionMatrices[index] = newMatrix;
        }

}
