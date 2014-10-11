package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

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
            if (layer != null)
                layerMap.put(node, layer);
        }
        
        // add the output layers to the suitable layers
        network.setLayerMap(layerMap);
        network.addOutputLayers(outputBuilder, outputLayerActivation, maxWindowSize, outputLayerHeight, allLevel, lexical);
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
            if (height >= 1 && (outputLayerHeight == -1 || height <= outputLayerHeight)) {
                if (!allLevel && (height != outputLayerHeight || (outputLayerHeight == -1 && node != parseTree))) {
                    continue;
                } else if (height == 1 && !lexical) {
                    continue;
                } else {
                    // TODO: put it back when doing preprocessing
                    // two steps for pre-processing:
                    // - putting the information to the terminal node
                    // - removing one branch node
                    Layer layer = layerMap.get(node);
//                    if (layer instanceof ProjectionLayer) continue;
//                    HiddenLayer hiddenLayer = (HiddenLayer) layer;
                    int windowSize = random.nextInt(maxWindowSize) + 1;
                    for (int i = node.getLeftmostPosition() - windowSize; i <= node.getRightmostPosition() + windowSize; i++) {
                        if ((i >= 0 && i < sentence.length && (i < node.getLeftmostPosition() || i > node.getRightmostPosition()))) {
                            // TODO: do something if word is not in Vocab
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
                
            } else {
                continue;
            }
        }
    }
    
    public void addHiddenLayer(HiddenLayer layer, int compositionIndex) {
        hiddenLayers.add(layer);
        compositionMatrixIndices.add(compositionIndex);
    }
    
    public void addProjectionLayer(ProjectionLayer layer, int inputVectorIndex) {
        projectionLayers.add(layer);
        inputVectorIndices.add(inputVectorIndex);
    }
    
    public void addOutputLayer(OutputLayer layer, int[] outVectorIndex) {
        outputLayers.add(layer);
        outVectorIndices.add(outVectorIndex);
    }
    
    
    public void learn(double learningRate) {
//        System.out.println(this);
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
    
    public void update(double learningRate) {
        for (int i = 0; i < projectionLayers.size(); i++) {
            int wordIndex = inputVectorIndices.get(i);
            SimpleMatrix gradient = projectionLayers.get(i).getGradient();
            projectionBuilder.updateVector(wordIndex, 
                    gradient, learningRate);
        }
        
        ArrayList<SimpleMatrix> hiddenGradients = new ArrayList<>();
        for (Layer layer: hiddenLayers) {
            hiddenGradients.add(layer.getGradient());
        }
        hiddenBuilder.updateMatrices(compositionMatrixIndices, hiddenGradients, learningRate);
        
        for (int i = 0; i < outputLayers.size(); i++) {
            outputBuilder.updateMatrix(outVectorIndices.get(i), 
                    outputLayers.get(i).getGradient(), learningRate);
        }
    }
    
    protected void setLayerMap(HashMap<Tree, Layer> layerMap) {
        this.layerMap = layerMap;
    }
    
    public String toString() {
        return "";//((BasicLayer) layerMap.get(parseTree)).toTreeString();
    }
    
}
