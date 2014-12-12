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

import common.MathUtils;

import tree.Tree;
import vocab.Vocab;

public class IncrementalTreeNetwork extends TreeNetwork{

    protected IncrementalTreeNetwork(Tree parseTree) {
        super(parseTree);
        // TODO Auto-generated constructor stub
    }
    
    // trees should already have updated height & width information
    // left position should be the length of history
    // concatenating concatenating 
    public static IncrementalTreeNetwork createNetwork(Tree parseTree, Tree rootTree, 
            String[] historyPresentFuture, Vocab vocab, ProjectionMatrix projectionBuilder, 
            CompositionMatrices hiddenBuilder, LearningStrategy outputBuilder,
            ActivationFunction hiddenLayerActivation, ActivationFunction outputLayerActivation,
            int maxWindowSize, double subSample) {
        IncrementalTreeNetwork network = new IncrementalTreeNetwork(parseTree);
        network.projectionBuilder = projectionBuilder;
        network.hiddenBuilder = hiddenBuilder;
        network.outputBuilder = outputBuilder;
        
        
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
                        // TODO: fix here
                        if (childLayer == null) return null;
                        layer.addInLayer(childLayer);
                        childLayer.addOutLayer(layer);
                    }
                    network.addHiddenLayer((HiddenLayer) layer, compositionIndex);
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
        network.addOutputLayers(rootTree, historyPresentFuture, vocab, outputBuilder, outputLayerActivation, maxWindowSize, subSample);
        
        //TODO: remove
        network.treeMap = treeMap;
        
        return network;
    }
    
    protected void addOutputLayers(Tree rootTree, String[] historyPresentFuture, Vocab vocab, LearningStrategy outputBuilder, 
            ActivationFunction outputLayerActivation, int maxWindowSize, double subSample) {

        // get the 
        String[] sentence = historyPresentFuture;
        Random random = new Random();
        
        Tree node = parseTree;
        int width = node.getRightmostPosition() - node.getLeftmostPosition() + 1;
        int height = node.getHeight();
        Layer layer = layerMap.get(node);
//        double significant = 1 / (double) width;
        double significant = 1;
        double inputCoefficient = 1 / (double) width;
        int windowSize = random.nextInt(maxWindowSize + height - 1) + 1;
//            int windowSize = maxWindowSize;
        
        // get the left and right position of the phrase
        // pick k words to the left and k words to the right to train the phrase 
        // (k = windowSize)
        for (int i = node.getLeftmostPosition() - windowSize; i <= node.getRightmostPosition() + windowSize; i++) {
            if ((i >= 0 && i < sentence.length && (i < node.getLeftmostPosition() || i > node.getRightmostPosition()))) {
                
                // adding the output layers to the hidden/projection layer 
                // corresponding to the phrase 
             
                int[] indices = outputBuilder.getOutputIndices(sentence[i]);
                if (indices == null) continue;
                // subSample
                long frequency = vocab.getEntry(sentence[i]).frequency;
                long totalCount = vocab.getTrainWords();
                if (subSample >0 && !MathUtils.isSampled(frequency, totalCount, subSample)) continue;
                
                
                SimpleMatrix weightMatrix = outputBuilder.getOutputWeights(indices);
                SimpleMatrix goldMatrix = outputBuilder.getGoldOutput(sentence[i]);
                
                ObjectiveFunction costFunction = outputBuilder.getCostFunction();
                OutputLayer outputLayer = new OutputLayer(weightMatrix, outputLayerActivation, goldMatrix, costFunction, significant, inputCoefficient);
                outputLayer.addInLayer(layer);
                layer.addOutLayer(outputLayer);
                
                addOutputLayer(outputLayer, indices);
            }
            
        }
    }
        
   
}
