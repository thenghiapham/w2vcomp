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
    protected HashMap<Tree, Layer> layerMap;
    public TreeNetwork(Tree parseTree, Vocab vocab, LearningStrategy strategy) {
        this.parseTree = parseTree;
        this.vocab = vocab;
        this.strategy = strategy;
        createNetwork();
    }
    
    public void createNetwork() {
        
    }
    
//    public void
    
    public void forward() {
        
    }
    
    public void backward() {
        
    }
    
    public void update() {
        
    }
}
