package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import neural.function.ActivationFunction;
import neural.layer.DiagonalHiddenLayer;
import neural.layer.Layer;
import neural.layer.ProjectionLayer;

import org.ejml.simple.SimpleMatrix;

import tree.Tree;

/**
 * This class represent a tree neural network for composing phrase vectors
 * when evaluating the composition
 * It simplifies available code of TreeNetwork by removing code used in learning
 * constructions' matrices and word vector representations
 * @author pham
 *
 */
public class SimpleDiagonalTreeNetwork {
    
    protected Tree parseTree;
    protected ArrayList<ProjectionLayer> projectionLayers;
    protected ArrayList<DiagonalHiddenLayer> hiddenLayers;
    protected ArrayList<Integer> compositionMatrixIndices;
    protected ArrayList<Integer> inputVectorIndices;
    

    protected SimpleDiagonalTreeNetwork(Tree parseTree) {
        this.parseTree = parseTree;
        projectionLayers = new ArrayList<>();
        hiddenLayers = new ArrayList<>();
        compositionMatrixIndices = new ArrayList<>();
        inputVectorIndices = new ArrayList<>();
    }
    
    public static SimpleDiagonalTreeNetwork createComposingNetwork(Tree parseTree,
            ProjectionMatrix projectionBuilder,
            DiagonalCompositionMatrices hiddenBuilder, ActivationFunction hiddenLayerActivation) {
        // TODO Auto-generated method stub
        SimpleDiagonalTreeNetwork network = new SimpleDiagonalTreeNetwork(parseTree);
        parseTree.updatePosition(0);
        
        HashMap<Tree, Layer> layerMap = new HashMap<>();
        
        ArrayList<Tree> reverseNodeList = parseTree.allNodes();
        Collections.reverse(reverseNodeList);
        for (Tree node: reverseNodeList) {
            Layer layer = null;
            if (node.isPreTerminal()) {
                // create projection layer with lexical vector as input
                Tree terminalChild = node.getChildren().get(0);
                String word = terminalChild.getRootLabel();
                int wordIndex = projectionBuilder.getWordIndex(word);
                SimpleMatrix vector = projectionBuilder.getVector(word);
                layer = new ProjectionLayer(vector);
                network.addProjectionLayer((ProjectionLayer) layer, wordIndex);
            } else if (node.isTerminal()) {
                // do nothing
            } else {
                ArrayList<Tree> children = node.getChildren();
                
                if (children.size() == 1) {
                    // if node has one child, the vector at this node points
                    // to the vector at the child node
                    layer = layerMap.get(children.get(0));
                } else {
                    // if node has more than one child, create a hidden layer
                    // with the layers at the child nodes as input
                    String construction = node.getConstruction();
                    SimpleMatrix weights = hiddenBuilder.getCompositionMatrix(construction);
                    int compositionIndex = hiddenBuilder.getConstructionIndex(construction);
                    layer = new DiagonalHiddenLayer(weights, hiddenLayerActivation);
                    for (Tree child: children) {
                        Layer childLayer = layerMap.get(child);
                        layer.addInLayer(childLayer);
                        childLayer.addOutLayer(layer);
                    }
                    network.addHiddenLayer((DiagonalHiddenLayer) layer, compositionIndex);
                }
            }
            
            // layerMap keeps track of the layers at the nodes
            if (layer != null) {
                layerMap.put(node, layer);
            }
        }
        return network;
    }
    
    protected void addHiddenLayer(DiagonalHiddenLayer layer, int compositionIndex) {
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
        return hiddenLayers.get(hiddenLayers.size() - 1).getOutput();
    }
    

}
