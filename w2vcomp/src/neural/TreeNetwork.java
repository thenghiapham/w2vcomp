package neural;

import java.util.ArrayList;
import java.util.HashMap;

import tree.Tree;
import vocab.Vocab;

public class TreeNetwork {
    protected Tree parseTree;
    protected Vocab vocab;
    LearningStrategy strategy;
    
    protected ArrayList<ProjectionLayer> projectionLayers;
    protected ArrayList<HiddenLayer> hiddenLayers;
    protected ArrayList<OutputLayer> outputLayers;
    protected ArrayList<Integer> compositionMatrixIndices;
    protected ArrayList<int[]> inputVectorIndices;
    protected ArrayList<int[]> outputVectorIndices;
    protected HashMap<Tree, Layer> layerMap;
    public TreeNetwork(Tree parseTree, Vocab vocab, LearningStrategy strategy) {
        this.parseTree = parseTree;
        this.vocab = vocab;
        this.strategy = strategy;
        createNetwork();
    }
    
    public void createNetwork() {
        
    }
    
    public void learn() {
        forward();
        backward();
        update();
    }
    
//    public void
    
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
    
    public void update() {
        
    }
}
