package parallel.workers.w2v;

import java.util.List;

import parallel.workers.ModelParameters;

public class SkipGramParametersDelta implements ModelParameters {

    // for sending back to the estimator
    double                    alpha;

    // from the estimator to the aggregator
    long                      wordCount;

    // the gradient from the estimator
    // the updated valua from the aggregator
    protected double[][]      weights0, weights1;

    List<Integer>             words_ids0, words_ids1;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SkipGramParametersDelta(double alpha, long wordCount,
            double[][] weights0, double[][] weights1, List<Integer> words_ids0,
            List<Integer> words_ids1) {
        this.alpha = alpha;
        this.wordCount = wordCount;
        this.weights0 = weights0;
        this.weights1 = weights1;
        this.words_ids0 = words_ids0;
        this.words_ids1 = words_ids1;
    }

}
