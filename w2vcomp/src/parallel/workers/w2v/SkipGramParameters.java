package parallel.workers.w2v;

import java.io.Serializable;

import parallel.workers.ModelParameters;

public class SkipGramParameters implements ModelParameters{
    
    // for sending back to the estimator
    double alpha;
    
    // from the estimator to the aggregator
    long wordCount;
    
    // the gradient from the estimator
    // the updated valua from the aggregator
    protected double[][] weights0, weights1;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SkipGramParameters(double alpha, long wordCount, double[][] weights0, double[][] weights1) {
        this.alpha = alpha;
        this.wordCount = wordCount;
        this.weights0 = weights0;
        this.weights1 = weights1;
    }
    
    @Override
    public Serializable getValue() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public void setValue(Serializable value) {
        // TODO Auto-generated method stub
        SkipGramParameters newParameter = (SkipGramParameters) value;
        this.alpha = newParameter.alpha;
        this.wordCount = newParameter.wordCount;
        this.weights0 = newParameter.weights0;
        this.weights1 = newParameter.weights1;
    }

}
