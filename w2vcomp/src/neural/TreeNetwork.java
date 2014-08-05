package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
            ActivationFunction hiddenLayerActivation) {
        TreeNetwork network = new TreeNetwork(parseTree);
        
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
                ArrayList<Tree> children = parseTree.getChildren();
                if (children.size() == 1) {
                    layer = layerMap.get(children.get(0));
                } else {
                    String construction = parseTree.getConstruction();
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
            if (layer != null)
                layerMap.put(node, layer);
        }
        // TODO:?
        network.setLayerMap(layerMap);
        
        return network;
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
            projectionBuilder.updateVector(inputVectorIndices.get(i), 
                    projectionLayers.get(i).getGradient(), learningRate);
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
    
}
