package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import neural.function.ActivationFunction;
import neural.function.ObjectiveFunction;
import neural.layer.HiddenLayer;
import neural.layer.Layer;
import neural.layer.OutputLayer;
import neural.layer.ProjectionLayer;

import org.ejml.simple.SimpleMatrix;

import tree.Tree;

/**
 * TreeNetwork class
 * creates a neural network from a parse tree
 * 
 * @author pham
 *
 */
public class TreeNetwork {
    private static final Logger LOGGER = Logger.getLogger(TreeNetwork.class.getName());
    
    protected Tree parseTree;
    
    /*
     * layers in the local network 
     */
    protected ArrayList<ProjectionLayer> projectionLayers;
    protected ArrayList<HiddenLayer> hiddenLayers;
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
    protected TreeNetwork(Tree parseTree) {
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
    public static TreeNetwork createNetwork(Tree parseTree, ProjectionMatrix projectionBuilder, 
            CompositionMatrices hiddenBuilder, LearningStrategy outputBuilder,
            ActivationFunction hiddenLayerActivation, ActivationFunction outputLayerActivation,
            int maxWindowSize, int outputLayerHeight, boolean allLevel, boolean lexical) {
//        LOGGER.log(Level.FINE, parseTree.toPennTree());
//        System.out.println("parse tree: " + parseTree.toPennTree());
        // setting global references
        TreeNetwork network = new TreeNetwork(parseTree);
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
                        
                        layer = new HiddenLayer(weights, hiddenLayerActivation);
                        for (Tree child: children) {
                            Layer childLayer = layerMap.get(child);
                            layer.addInLayer(childLayer);
                            childLayer.addOutLayer(layer);
                        }
                        network.addHiddenLayer((HiddenLayer) layer, compositionIndex);
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
            
            if (!allLevel) {
                if (outputLayerHeight != -1 && height != outputLayerHeight)
                    continue;
                if (!lexical && height == 1) continue;
            } else {
                if (!lexical && height == 1) continue;
            }
            
            Layer layer = layerMap.get(node);
            int windowSize = random.nextInt(maxWindowSize) + 1;
            
            // get the left and right position of the phrase
            // pick k words to the left and k words to the right to train the phrase 
            // (k = windowSize)
            for (int i = node.getLeftmostPosition() - windowSize; i <= node.getRightmostPosition() + windowSize; i++) {
                if ((i >= 0 && i < sentence.length && (i < node.getLeftmostPosition() || i > node.getRightmostPosition()))) {
                    
                    // adding the output layers to the hidden/projection layer 
                    // corresponding to the phrase 
                    int[] indices = outputBuilder.getOutputIndices(sentence[i]);
                    if (indices == null) continue;
                    SimpleMatrix weightMatrix = outputBuilder.getOutputWeights(indices);
                    SimpleMatrix goldMatrix = outputBuilder.getGoldOutput(sentence[i]);
                    
                    ObjectiveFunction costFunction = outputBuilder.getCostFunction();
                    OutputLayer outputLayer = new OutputLayer(weightMatrix, outputLayerActivation, goldMatrix, costFunction);
                    outputLayer.addInLayer(layer);
                    layer.addOutLayer(outputLayer);
                    
                    addOutputLayer(outputLayer, indices);
                }
            }
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
    public void addHiddenLayer(HiddenLayer layer, int compositionIndex) {
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
        hiddenBuilder.updateMatrices(compositionMatrixIndices, hiddenGradients, learningRate);
        
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
        return "";//((BasicLayer) layerMap.get(parseTree)).toTreeString();
    }
    
}
