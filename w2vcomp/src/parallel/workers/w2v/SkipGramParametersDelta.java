package parallel.workers.w2v;

import java.util.List;

import parallel.workers.ModelParameters;

// from the estimator to the aggregator
public class SkipGramParametersDelta implements ModelParameters {

    long                      wordCount;

    SkipGramParametersSubset parametersSubset;

    private static final long serialVersionUID = 1L;

    public SkipGramParametersDelta(long wordCount,
            SkipGramParametersSubset parametersSubset) {
        this.wordCount = wordCount;
        this.parametersSubset = parametersSubset;
    }

    public double[][] getWeights0() {
        return parametersSubset.getWeights0();
    }

    public void setWeights0(double[][] weights0) {
        this.parametersSubset.setWeights0(weights0);
    }

    public double[][] getWeights1() {
        return parametersSubset.getWeights1();
    }

    public void setWeights1(double[][] weights1) {
        this.parametersSubset.setWeights1(weights1);
    }

    public double getAlpha() {
        return this.parametersSubset.getAlpha();
    }

    public void setAlpha(double alpha) {
        this.parametersSubset.setAlpha(alpha);
    }

    public List<Integer> getWordsIds0() {
        return this.parametersSubset.getWordsIds0();
    }

    public void setWordsIds0(List<Integer> wordsIds0) {
        this.parametersSubset.setWordsIds0(wordsIds0);
    }

    public List<Integer> getWordsIds1() {
        return this.parametersSubset.getWordsIds1();
    }

    public void setWordsIds1(List<Integer> wordsIds1) {
        this.parametersSubset.setWordsIds1(wordsIds1);
    }
}
