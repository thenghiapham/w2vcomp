package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import neural.function.ActivationFunction;
import neural.function.ObjectiveFunction;
import neural.layer.BasicLayer;
import neural.layer.Layer;
import neural.layer.OutputLayer;
import neural.layer.ProjectionLayer;
import neural.layer.WeightedHiddenLayer;

import org.ejml.simple.SimpleMatrix;


import tree.Tree;

/**
 * TreeNetwork class
 * creates a neural network from a parse tree
 * 
 * @author pham
 *
 */
public class WeightedTreeNetwork {
    private static final Logger LOGGER = Logger.getLogger(WeightedTreeNetwork.class.getName());
    private static final double epsilon = 1e-4;
//    private static final double LEVEL_DECAY = 0.1;
    
    protected Tree parseTree;
    
    /*
     * layers in the local network 
     */
    protected ArrayList<ProjectionLayer> projectionLayers;
    protected ArrayList<WeightedHiddenLayer> hiddenLayers;
    protected ArrayList<OutputLayer> outputLayers;
    
    /*
     * indices of the matrices, vectors to update
     */
    protected ArrayList<Integer> compositionMatrixIndices;
    protected ArrayList<Integer> inputVectorIndices;
    protected ArrayList<int[]> outVectorIndices;
    
    /*
     * references to global data 
     */
    protected ProjectionMatrix projectionBuilder;
    protected CompositionMatrices hiddenBuilder;
    protected LearningStrategy outputBuilder;
    HashMap<Tree, Layer> layerMap;
    
    // for debuging
    // TODO: remove
    HashMap<Layer, Tree> treeMap;
    
    /**
     * Constructor
     * Create an empty network
     * @param parseTree
     */
    protected WeightedTreeNetwork(Tree parseTree) {
        this.parseTree = parseTree;
        projectionLayers = new ArrayList<>();
        hiddenLayers = new ArrayList<>();
        outputLayers = new ArrayList<>();
        compositionMatrixIndices = new ArrayList<>();
        inputVectorIndices = new ArrayList<>();
        outVectorIndices = new ArrayList<>();
        
    }
    
    /**
     * Creating a neural network from global data & the parse tree
     *   - build the layers bottom up (until a certain level)
     *   - add the output layers to all the hidden layers (and projection layers) 
     * @param parseTree
     * @param projectionBuilder
     * @param hiddenBuilder
     * @param outputBuilder
     * @param hiddenLayerActivation
     * @param outputLayerActivation
     * @param maxWindowSize: window size
     * @param outputLayerHeight
     * @param allLevel
     * @param lexical
     * @return
     * TODO: add condition for adding surrounding context
     * (just a matter of adding previous & next sentence as Strings)
     */
    public static WeightedTreeNetwork createNetwork(Tree parseTree, ProjectionMatrix projectionBuilder, 
            WeightedCompositionMatrices hiddenBuilder, LearningStrategy outputBuilder,
            ActivationFunction hiddenLayerActivation, ActivationFunction outputLayerActivation,
            int maxWindowSize, int outputLayerHeight, boolean allLevel, boolean lexical) {
//        LOGGER.log(Level.FINE, parseTree.toPennTree());
//        System.out.println("parse tree: " + parseTree.toPennTree());
        // setting global references
        WeightedTreeNetwork network = new WeightedTreeNetwork(parseTree);
        network.projectionBuilder = projectionBuilder;
        network.hiddenBuilder = hiddenBuilder;
        network.outputBuilder = outputBuilder;
        
        // adding information about height 
        // & position (left and right position) in the sentence
        // to each node of the parse tree
        // TODO: unit test here
        parseTree.updateHeight();
        parseTree.updatePosition(0);
        
        HashMap<Tree, Layer> layerMap = new HashMap<>();
        
        //TODO: remove
        HashMap<Layer, Tree> treeMap = new HashMap<>();
        
        // create a list of node in the parse Tree
        // from bottom to top
        ArrayList<Tree> reverseNodeList = parseTree.allNodes();
        Collections.reverse(reverseNodeList);
        
        
        // creating projection layers &  hidden layers
        // corresponding to each node in the parse tree
        // from child nodes to parent nodes
        for (Tree node: reverseNodeList) {
            Layer layer = null;
            if (node.isPreTerminal()) {
                // create projection layers at the preterminal nodes
                Tree terminalChild = node.getChildren().get(0);
                String word = terminalChild.getRootLabel();
                int wordIndex = projectionBuilder.getWordIndex(word);
                SimpleMatrix vector = projectionBuilder.getVector(word);
                
                layer = new ProjectionLayer(vector);
                network.addProjectionLayer((ProjectionLayer) layer, wordIndex);
            } else if (node.isTerminal()) {
                // do nothing for terminal nodes
            } else {
                // only create the hidden layer for nodes that has a certain height
                // (i.e. nodes that will be attached to an output layer)
                if (outputLayerHeight == -1 || node.getHeight() <= outputLayerHeight) {
                    ArrayList<Tree> children = node.getChildren();
                    if (children.size() == 1) {
                        // if the node has one child (type raising, etc)
                        // map the node's layer to the child's layer
                        layer = layerMap.get(children.get(0));
                    } else {
                        // if a node has >= 2 children (2 in binary tree)
                        // create a new layer
                        // attach its childen' layers to the new layer as input
                        // the input weight matrix is taken from the corresponding
                        // construction
                        String construction = node.getConstruction();
                        SimpleMatrix weights = hiddenBuilder.getCompositionMatrix(construction);
                        int compositionIndex = hiddenBuilder.getConstructionIndex(construction);
                        
                        layer = new WeightedHiddenLayer(weights, hiddenLayerActivation);
                        for (Tree child: children) {
                            Layer childLayer = layerMap.get(child);
                            // TODO: fix here
                            if (childLayer == null) return null;
                            layer.addInLayer(childLayer);
                            childLayer.addOutLayer(layer);
                        }
                        network.addHiddenLayer((WeightedHiddenLayer) layer, compositionIndex);
                    }
                }
            }
            if (layer != null) {
                layerMap.put(node, layer);
                
                //TODO: remove
                treeMap.put(layer, node);
            }
                
        }
        
        // add the output layers to the suitable layers
        network.setLayerMap(layerMap);
        network.addOutputLayers(outputBuilder, outputLayerActivation, maxWindowSize, outputLayerHeight, allLevel, lexical);
        
        //TODO: remove
        network.treeMap = treeMap;
        
        return network;
    }
    
    protected void addOutputLayers(LearningStrategy outputBuilder, ActivationFunction outputLayerActivation, int maxWindowSize, 
            int outputLayerHeight, boolean allLevel, boolean lexical) {

        // get the 
        String[] sentence = parseTree.getSurfaceWords();
        Random random = new Random();

        // going through the nodes that have a projection layer or hidden layer 
        for (Tree node: layerMap.keySet()) {
            int height = node.getHeight();
            int width = node.getRightmostPosition() - node.getLeftmostPosition() + 1;
            
            if (!allLevel) {
                if (outputLayerHeight != -1 && height != outputLayerHeight)
                    continue;
                if (!lexical && height == 1) continue;
            } else {
                if (!lexical && height == 1) continue;
            }
            
            Layer layer = layerMap.get(node);
            // TODO: change back?
//            double coefficient = Math.pow(LEVEL_DECAY, height - 1);
//            double coefficient = 1;
            double coefficient = 1 / (double) width;
            
            int windowSize = random.nextInt(maxWindowSize) + 1;
            // TODO: turn back to random
//            int windowSize = maxWindowSize;
            
            // get the left and right position of the phrase
            // pick k words to the left and k words to the right to train the phrase 
            // (k = windowSize)
            for (int i = node.getLeftmostPosition() - windowSize; i <= node.getRightmostPosition() + windowSize; i++) {
                if ((i >= 0 && i < sentence.length && (i < node.getLeftmostPosition() || i > node.getRightmostPosition()))) {
                    
                    // adding the output layers to the hidden/projection layer 
                    // corresponding to the phrase 
                    int[] indices = outputBuilder.getOutputIndices(sentence[i]);
//                    IOUtils.printInts(indices);
//                    System.out.println("****");
                    if (indices == null) continue;
                    SimpleMatrix weightMatrix = outputBuilder.getOutputWeights(indices);
                    SimpleMatrix goldMatrix = outputBuilder.getGoldOutput(sentence[i]);
                    
                    ObjectiveFunction costFunction = outputBuilder.getCostFunction();
                    OutputLayer outputLayer = new OutputLayer(weightMatrix, outputLayerActivation, goldMatrix, costFunction, coefficient);
                    outputLayer.addInLayer(layer);
                    layer.addOutLayer(outputLayer);
                    
                    addOutputLayer(outputLayer, indices);
                }
                
            }
//            System.exit(0);
        }
        
    }
    
    
    /**METHODS TO KEEP TRACK OF DIFFERENT TYPES OF LAYER**/
    // This is necessary because layers of different types are used to
    // update different components (projectionMatrix, compositionMatrix,
    // and softmax/negativeSampling)
    
    /**
     * keep track of the hidden layer
     * @param layer
     * @param compositionIndex
     */
    public void addHiddenLayer(WeightedHiddenLayer layer, int compositionIndex) {
        hiddenLayers.add(layer);
        compositionMatrixIndices.add(compositionIndex);
    }
    
    /**
     * keep track of the projection layer
     * @param layer
     * @param compositionIndex
     */
    public void addProjectionLayer(ProjectionLayer layer, int inputVectorIndex) {
        projectionLayers.add(layer);
        inputVectorIndices.add(inputVectorIndex);
    }
    
    /**
     * keep track of the output layer
     * @param layer
     * @param outVectorIndex
     */
    public void addOutputLayer(OutputLayer layer, int[] outVectorIndex) {
        outputLayers.add(layer);
        outVectorIndices.add(outVectorIndex);
    }
    
    
    /**METHODS FOR LEARNING**/
    /**
     * train the network with the parseTree
     * it involves 3 steps
     *   - forward (to compute output)
     *   - backward (to back-propagate errors)
     *   - update the weights
     * @param learningRate
     */
    public void learn(double learningRate) {
        forward();
        backward();
        update(learningRate);
//        System.exit(0);
    }
    
    public void forward() {
        // forward in an order which
        // the child layers precede the father layers
        for (Layer layer: projectionLayers) {
            layer.forward();
        }
        for (Layer layer: hiddenLayers) {
            layer.forward();
        }
        for (Layer layer: outputLayers) {
            layer.forward();
        }
    }
    
    public void backward() {
        // forward in an order which
        // the child layers succeed the father layers
        for (Layer layer: outputLayers) {
            layer.backward();
        }
        for (int i = hiddenLayers.size() - 1; i > -1; i--) {
            hiddenLayers.get(i).backward();
        }
        for (Layer layer: projectionLayers) {
            layer.backward();
        }
    }
    
    /**
     * Updating the global components
     * @param learningRate
     */
    public void update(double learningRate) {
        
        // updating the projection matrix
        for (int i = 0; i < projectionLayers.size(); i++) {
            int wordIndex = inputVectorIndices.get(i);
            SimpleMatrix gradient = projectionLayers.get(i).getGradient();
            if (gradient == null) {
//                System.out.println(" empty " + treeMap.get(projectionLayers.get(i)).toPennTree());
                //LOGGER.log(Level.FINE, treeMap.get(projectionLayers.get(i)).toPennTree());
                continue;
            }
            projectionBuilder.updateVector(wordIndex, 
                    gradient, learningRate);
        }
        
        // updating the compositionMatrices
        ArrayList<SimpleMatrix> hiddenGradients = new ArrayList<>();
        for (Layer layer: hiddenLayers) {
            hiddenGradients.add(layer.getGradient());
        }
        
        //TODO: change learning rate back
        hiddenBuilder.updateMatrices(compositionMatrixIndices, hiddenGradients, learningRate * 0.001);
        
        // updating the hierarchical softmax or the negative sampling layer
        for (int i = 0; i < outputLayers.size(); i++) {
            outputBuilder.updateMatrix(outVectorIndices.get(i), 
                    outputLayers.get(i).getGradient(), learningRate);
        }
    }
    
    /**GET/SET METHODS**/
    protected void setLayerMap(HashMap<Tree, Layer> layerMap) {
        this.layerMap = layerMap;
    }
    
    /**DEBUGGING METHODS**/
    public String toString() {
        // TODO: print the whole network here
        return toPennTree(parseTree);
    }
    
    protected String toPennTree(Tree node) {
        String treeString = "("+getLayerString(node);
        ArrayList<Tree> children = node.getChildren();
        if (children.size() == 1) {
            treeString += " ";
            if (children.get(0).getChildren().size() == 0)
                treeString += getLayerString(children.get(0));
            else
                treeString += toPennTree(children.get(0));
        }
        else if (children.size() > 1) {
            treeString += " ";
            for (Tree child : children)
                treeString += toPennTree(child);
        }
        treeString += ")";
        return treeString;
    }
    
    protected String getLayerString(Tree node) {
        if (!layerMap.containsKey(node)) {
            if (node.isTerminal()) return node.getRootLabel();
            else return "N";
        }
        else {
            BasicLayer layer = (BasicLayer) layerMap.get(node);
            String result = layer.getTypeString() + node.getHeight();
            if (layer.getOutSize() >= 2)
                result = result + "*" + layer.getOutSize();
            return result;
        }
    }
    
    /**CHECKING GRADIENTS**/
    public void checkGradient() {
        ArrayList<SimpleMatrix> realGradients = computeRealGradient();
        ArrayList<SimpleMatrix> numericGradients = computeNumericGradient();
        for (int i = 0; i < realGradients.size(); i++) {
            
            SimpleMatrix component = realGradients.get(i);
            SimpleMatrix numComponent = numericGradients.get(i);
            if (component == null) {
                component = new SimpleMatrix(numComponent.numRows(), numComponent.numCols());
            }
            
            double squareError = component.minus(numComponent).normF();
            squareError = squareError * squareError;
            if (squareError / (component.numCols() * component.numRows()) > 1e-5) {
                LOGGER.log(Level.FINE, "Big error " + squareError / (component.numCols() * component.numRows()));
//                System.out.println(squareError / (component.numCols() * component.numRows()));
//                System.out.println("Big error " + getLayerType(i));
//                System.out.println("real");
//                System.out.println(component);
//                System.out.println("num");
//                System.out.println(numComponent);
//                System.out.println("layer");
//                System.out.println(getLayer(i).getWeights());
            } else {
//                System.out.println(squareError / (component.numCols() * component.numRows()));
//                System.out.println("Good error " + getLayerType(i));
            }
        }
    }
    
    protected ArrayList<SimpleMatrix> computeRealGradient() {
        forward();
        backward();
        
        ArrayList<SimpleMatrix> gradients = new ArrayList<SimpleMatrix>();
        // loop through the layers to get the gradients
        for (ProjectionLayer layer: projectionLayers) {
            gradients.add(layer.getGradient());
        }
        for (WeightedHiddenLayer layer: hiddenLayers) {
            gradients.add(layer.getGradient());
        }
        for (OutputLayer layer: outputLayers) {
            gradients.add(layer.getGradient());
        }
        return gradients;
    }
    
    public double computeCost() {
        forward();
        double cost = 0;
        for (OutputLayer layer: outputLayers) {
            cost += layer.getCost();
        }
        return cost;
    }
    
    protected ArrayList<SimpleMatrix> computeNumericGradient() {
        ArrayList<SimpleMatrix> gradients = new ArrayList<SimpleMatrix>();
        int numLayers = projectionLayers.size() + hiddenLayers.size() + outputLayers.size();
        for (int i = 0; i < numLayers; i++) {
            SimpleMatrix gradient = computeNumericGradient(i);
            gradients.add(gradient);
        }
        return gradients;
    }
    
    protected SimpleMatrix computeNumericGradient(int i) {
        // find the layer that correspond to the index i
        // compute the numeric graident of that layer
        BasicLayer layer = getLayer(i);
        SimpleMatrix weights = layer.getWeights();
        SimpleMatrix tmpWeights = weights.copy();
        layer.setWeights(tmpWeights);
        SimpleMatrix delta = new SimpleMatrix(weights.numRows(), weights.numCols());
        int size = weights.numRows() * weights.numCols();
        for (int index = 0; index < size; index++) {
            double element = tmpWeights.get(index);
            tmpWeights.set(index, element + epsilon);
            double plusCost = computeCost();
            tmpWeights.set(index, element - epsilon);
            double minusCost = computeCost();
            delta.set(index, (plusCost - minusCost) / (2 * epsilon));
            tmpWeights.set(index, element);
        }
        layer.setWeights(weights);
        return delta;
    }
    
    protected BasicLayer getLayer(int i) {
        if (i < projectionLayers.size()) {
            return projectionLayers.get(i);
        }
        else {
            i = i - projectionLayers.size();
            if (i < hiddenLayers.size()) {
                return hiddenLayers.get(i);
            } else {
                i = i - hiddenLayers.size();
                return outputLayers.get(i);
            }
        }
    }
    
    protected String getLayerType(int i) {
        if (i < projectionLayers.size()) {
            return "P" + i;
        }
        else {
            i = i - projectionLayers.size();
            if (i < hiddenLayers.size()) {
                return "H" + i;
            } else {
                i = i - hiddenLayers.size();
                return "O" + i;
            }
        }
    }
}
