package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import tree.Tree;

public class TreeNetwork {
    protected Tree parseTree;
    protected ArrayList<ProjectionLayer> projectionLayers;
    protected ArrayList<HiddenLayer> hiddenLayers;
    protected ArrayList<OutputLayer> outputLayers;
    protected ArrayList<Integer> compositionMatrixIndices;
    protected ArrayList<Integer> inputVectorIndices;
    protected ArrayList<int[]> outVectorIndices;
    
    protected ProjectionMatrix projectionBuilder;
    protected CompositionMatrices hiddenBuilder;
    protected LearningStrategy outputBuilder;
    HashMap<Tree, Layer> layerMap;
    
    protected TreeNetwork(Tree parseTree) {
        this.parseTree = parseTree;
        projectionLayers = new ArrayList<>();
        hiddenLayers = new ArrayList<>();
        outputLayers = new ArrayList<>();
        compositionMatrixIndices = new ArrayList<>();
        inputVectorIndices = new ArrayList<>();
        outVectorIndices = new ArrayList<>();
        
    }
    
    
    public static TreeNetwork createNetwork(Tree parseTree, ProjectionMatrix projectionBuilder, 
            CompositionMatrices hiddenBuilder, LearningStrategy outputBuilder,
            ActivationFunction hiddenLayerActivation, ActivationFunction outputLayerActivation,
            int maxWindowSize, int outputLayerHeight, boolean allLevel, boolean lexical) {
        
        TreeNetwork network = new TreeNetwork(parseTree);
        network.projectionBuilder = projectionBuilder;
        network.hiddenBuilder = hiddenBuilder;
        network.outputBuilder = outputBuilder;
        
        
        parseTree.updateHeight();
        parseTree.updatePosition(0);
        
        HashMap<Tree, Layer> layerMap = new HashMap<>();
        
        ArrayList<Tree> reverseNodeList = parseTree.allNodes();
        Collections.reverse(reverseNodeList);
        for (Tree node: reverseNodeList) {
            Layer layer = null;
            if (node.isPreTerminal()) {
                Tree terminalChild = node.getChildren().get(0);
                String word = terminalChild.getRootLabel();
                int wordIndex = projectionBuilder.getWordIndex(word);
                SimpleMatrix vector = projectionBuilder.getVector(word);
                layer = new ProjectionLayer(vector);
                network.addProjectionLayer((ProjectionLayer) layer, wordIndex);
            } else if (node.isTerminal()) {
                
            } else {
                if (outputLayerHeight == -1 || node.getHeight() <= outputLayerHeight) {
                    ArrayList<Tree> children = node.getChildren();
                    if (children.size() == 1) {
                        layer = layerMap.get(children.get(0));
                    } else {
                        String construction = node.getConstruction();
//                        System.out.println(construction);
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
        // TODO:?
        network.setLayerMap(layerMap);
        network.addOutputLayers(outputBuilder, outputLayerActivation, maxWindowSize, outputLayerHeight, allLevel, lexical);
        return network;
    }
    
    protected void addOutputLayers(LearningStrategy outputBuilder, ActivationFunction outputLayerActivation, int maxWindowSize, 
            int outputLayerHeight, boolean allLevel, boolean lexical) {
        String[] sentence = parseTree.getSurfaceWords();
        Random random = new Random();
        for (Tree node: layerMap.keySet()) {
            // TODO: changable here to 1 if one to include Mikolov's skipgram
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
        hiddenBuilder.updateConstructions(compositionMatrixIndices, hiddenGradients, learningRate);
        
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
