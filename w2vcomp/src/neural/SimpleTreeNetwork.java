package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

import tree.Tree;

public class SimpleTreeNetwork {
    
    protected Tree parseTree;
    protected ArrayList<ProjectionLayer> projectionLayers;
    protected ArrayList<HiddenLayer> hiddenLayers;
    protected ArrayList<Integer> compositionMatrixIndices;
    protected ArrayList<Integer> inputVectorIndices;
    

    protected SimpleTreeNetwork(Tree parseTree) {
        this.parseTree = parseTree;
        projectionLayers = new ArrayList<>();
        hiddenLayers = new ArrayList<>();
        compositionMatrixIndices = new ArrayList<>();
        inputVectorIndices = new ArrayList<>();
    }
    
    public static SimpleTreeNetwork createComposingNetwork(Tree parseTree,
            ProjectionMatrix projectionBuilder,
            CompositionMatrices hiddenBuilder, ActivationFunction hiddenLayerActivation) {
        // TODO Auto-generated method stub
        SimpleTreeNetwork network = new SimpleTreeNetwork(parseTree);
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
                
                    ArrayList<Tree> children = node.getChildren();
                    if (children.size() == 1) {
                        layer = layerMap.get(children.get(0));
                    } else {
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
            if (layer != null)
                layerMap.put(node, layer);
        }
        return network;
    }
    
    protected void addHiddenLayer(HiddenLayer layer, int compositionIndex) {
        hiddenLayers.add(layer);
        compositionMatrixIndices.add(compositionIndex);
    }
    
    protected void addProjectionLayer(ProjectionLayer layer, int inputVectorIndex) {
        projectionLayers.add(layer);
        inputVectorIndices.add(inputVectorIndex);
    }

    public SimpleMatrix compose() {
        for (Layer layer: projectionLayers) {
            layer.forward();
        }
        for (Layer layer: hiddenLayers) {
            layer.forward();
        }
        return hiddenLayers.get(hiddenLayers.size() - 1).output;
    }
    

}
