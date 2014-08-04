package neural;

import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;
import word2vec.UniGram;

public class NegativeSamplingLayer {
    protected SimpleMatrix outputVectors;
    protected UniGram uniGram;
    protected Vocab vocab;
    protected int noSamples;
    
    protected NegativeSamplingLayer(Vocab vocab, int noSamples) {
        this.vocab = vocab;
        this.noSamples = noSamples;
        this.uniGram = new UniGram(vocab);
    }
    
    
    public SimpleMatrix getOutputWeights(String word) {
        
        return null;
    }
    
    public SimpleMatrix getGoldOutput(String word) {
        return null;
    }
}
