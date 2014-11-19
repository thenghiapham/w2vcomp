package parallel.workers.w2v;

import java.util.Random;

import common.SigmoidTable;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterAggregator;
import vocab.Vocab;
import word2vec.UniGram;

public class SkipGramAggregator implements ParameterAggregator{
    protected double           starting_alpha;
    protected double           alpha;

    /*
     * objective function options: - hierarchicalSoftmax:
     * log(p(output_word_code|input_words)) - negativeSampling
     */
    protected boolean          hierarchicalSoftmax;
    protected int              negativeSamples;

    protected double            subSample;

    // projection/hidden layer size
    protected int              projectionLayerSize;

    protected int              windowSize;

    protected Vocab            vocab;

    // parameters to keep track of the training progress
    protected long             wordCount;
    protected long             trainWords;

    // uniGram language model, used in negativeSampling method
    protected UniGram          unigram;
    // pre-computed sigmoid Table for fast computing sigmoid
    // while losing a bit of precision
    protected SigmoidTable     sigmoidTable;

    /*
     * weights of neural network: - weights0: projection matrices size = V * H,
     * only w * 2 * H are estimated at a time - weights1: projection layer to
     * output layer: hierarchical softmax size = H * (V - 1), only H * log(V)
     * are estimated - negativeWeights1: projection layer to output layer:
     * negative Sampling size = H * V, only (k + 1) * V are estimated, where k =
     * negativeSamples
     */
    double[][]                  weights0, weights1;
    
    @Override
    public ModelParameters aggregate(ModelParameters content) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModelParameters getInitParameters() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public ModelParameters getFinalParameters() {
        // TODO Auto-generated method stub
        return null;
    }

}
