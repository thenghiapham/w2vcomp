package parallel.workers.w2v;

import java.util.ArrayList;
import java.util.List;

import common.MathUtils;
import parallel.workers.ModelParameters;

public class SkipGramParametersSubset implements ModelParameters {

    // for sending back to the estimator
    private double            alpha;

    // the gradient from the estimator
    // the updated valua from the aggregator
    private double[][]        weights0;

    private double[][]        weights1;

    private List<Integer>     wordsIds0;

    private List<Integer>     wordsIds1;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SkipGramParametersSubset(double alpha, double[][] weights0,
            double[][] weights1, List<Integer> words_ids0,
            List<Integer> words_ids1) {
        this.setAlpha(alpha);
        this.weights0 = weights0;
        this.setWeights1(weights1);
        this.setWordsIds0(words_ids0);
        this.setWordsIds1(words_ids1);
    }
    
    public SkipGramParametersSubset(SkipGramParametersSubset ot) {
        this.setAlpha(ot.getAlpha());
        this.setWeights0(MathUtils.deepCopy(ot.getWeights0()));
        this.setWeights1(MathUtils.deepCopy(ot.getWeights1()));
        //Not neccessary, but here for consistency
        if (ot.getWordsIds0() != null)
            this.wordsIds0 = new ArrayList<Integer>(ot.getWordsIds0());
        if (ot.getWordsIds1() != null)
            this.wordsIds1 = new ArrayList<Integer>(ot.getWordsIds1());
    }

    public double[][] getWeights0() {
        return weights0;
    }

    public void setWeights0(double[][] weights0) {
        this.weights0 = weights0;
    }

    public double[][] getWeights1() {
        return weights1;
    }

    public void setWeights1(double[][] weights1) {
        this.weights1 = weights1;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public List<Integer> getWordsIds0() {
        return wordsIds0;
    }

    public void setWordsIds0(List<Integer> wordsIds0) {
        this.wordsIds0 = wordsIds0;
    }

    public List<Integer> getWordsIds1() {
        return wordsIds1;
    }

    public void setWordsIds1(List<Integer> wordsIds1) {
        this.wordsIds1 = wordsIds1;
    }

}
