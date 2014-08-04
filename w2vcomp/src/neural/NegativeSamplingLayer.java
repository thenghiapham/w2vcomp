package neural;

import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;
import word2vec.UniGram;

public class NegativeSamplingLayer {
    public SimpleMatrix outputVectors;
    public UniGram uniGram;
    protected Vocab vocab;
    
    public NegativeSamplingLayer() {
       
    }
    
    public SimpleMatrix getOutputWeights(String word) {
        return null;
    }
    
    public SimpleMatrix getGoldOutput(String word) {
        return null;
    }
}
