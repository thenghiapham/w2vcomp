package parallel.workers.w2v;

import java.util.Random;

import parallel.comm.ParameterMessager;
import space.RawSemanticSpace;
import vocab.Vocab;

import common.correlation.MenCorrelation;

import demo.TestConstants;

public class ParameterUpdatePoller {
    protected double       batch_size;
    // Number of times a word was updated to trigger an update
    protected final int    updateThreshold = 10;
    protected final double mean_batch      = 1.0 / 300000.0;
    protected final int    max_batch       = 2000000;
    protected long         oldWordCount;
    protected SkipGramParameters modelParams, oldParams;
    protected Integer            worker_id;
    protected ParameterMessager  parameterMessager;
    private Random               rand = new Random();

    MenCorrelation               men;

    public ParameterUpdatePoller(Integer worker_id,
            SkipGramParameters modelParams, ParameterMessager parameterMessager) {
        this.worker_id = worker_id;
        this.modelParams = modelParams;
        this.parameterMessager = parameterMessager;
        // Keep a copy
        this.oldParams = new SkipGramParameters(modelParams);
        oldWordCount = 0;
        resampleBatchSize();

        men = new MenCorrelation(TestConstants.S_MEN_FILE);
        System.out.println("Men size: " + men.getGolds().length);
    }

    private void resampleBatchSize() {
        batch_size = Math.min(Math.log(1 - rand.nextDouble()) / -mean_batch,
                max_batch);
    }

    public boolean checkUpdate(long wordCount, Vocab vocab) {
        if (wordCount - oldWordCount >= batch_size) {
            resampleBatchSize();
            if (rand.nextFloat() <= 0.999) {
                RawSemanticSpace space = new RawSemanticSpace(vocab,
                        modelParams.getWeights0(), false);
                System.out.println(men.evaluateSpacePearson(space));
            }
            System.out.println("vector: " + modelParams.getWeights0()[2][0] + " "
                    + modelParams.getWeights0()[2][1]);

            // Construct difference
            SkipGramParametersDelta deltaParams = modelParams.getDelta(
                    oldParams, wordCount - oldWordCount, updateThreshold);
            // Send and receive updated difference
            SkipGramParametersSubset newParams = (SkipGramParametersSubset) parameterMessager
                    .sendUpdate(worker_id, deltaParams).getContent();
            // apply the difference
            modelParams.setSubset(newParams);
            oldParams = new SkipGramParameters(modelParams);
            oldWordCount = wordCount;
            System.out.println("wordCount: " + wordCount);
            if (rand.nextFloat() <= 0.999) {
                RawSemanticSpace space = new RawSemanticSpace(vocab,
                        modelParams.getWeights0(), false);
                System.out.println(men.evaluateSpacePearson(space));
            }
            return true;
        }
        return false;
    }
}
