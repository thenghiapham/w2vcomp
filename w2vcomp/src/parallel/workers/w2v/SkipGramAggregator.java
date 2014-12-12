package parallel.workers.w2v;

import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections15.map.HashedMap;

import common.MathUtils;

import demo.TestConstants;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterAggregator;
import vocab.Vocab;

public class SkipGramAggregator implements ParameterAggregator {
    protected long             trainWords;
    double                     starting_alpha;
    long                       wordCount = 0;
    // Set of matrix entries that have to be updated
    // on each of the proccesses
    Map<Integer, Set<Integer>> to_update;
    // Model parameters
    SkipGramParameters         modelParams;

    public SkipGramAggregator() {
        starting_alpha = 0.025;
        wordCount = 0;
        Vocab vocab = new Vocab(RunningConstant.MIN_FREQUENCY);
        buildVocab(vocab, TestConstants.S_VOCABULARY_FILE);
        trainWords = vocab.getTrainWords();

        int vocabSize = vocab.getVocabSize();
        int projectionLayerSize = RunningConstant.VECTOR_SIZE;
        double[][] weights0 = new double[vocabSize][projectionLayerSize];
        Random rand = new Random();
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            for (int j = 0; j < projectionLayerSize; j++) {
                weights0[i][j] = (double) (rand.nextFloat() - 0.5)
                        / projectionLayerSize;
            }
        }
        double[][] weights1;
        if (RunningConstant.HIERARCHICAL_SOFTMAX) {
            weights1 = new double[vocabSize - 1][projectionLayerSize];
        } else if (RunningConstant.NEGATIVE_SAMPLES > 0) {
            weights1 = new double[vocabSize][projectionLayerSize];
        }
        vocab.assignCode();
        // Since it contains all the words in the vocabulary, we don't specify
        // the list of present words
        modelParams = new SkipGramParameters(starting_alpha, weights0, weights1);
        to_update = new HashedMap<>();
    }

    public void buildVocab(Vocab vocab, String vocabFile) {
        boolean learnVocab = !(new File(vocabFile)).exists();
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            try {
                File trainDir = new File(TestConstants.S_TRAIN_DIR);
                System.out.println("train dir: " + trainDir.getAbsolutePath());
                File[] trainFiles = trainDir.listFiles();
                ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
                for (File trainFile : trainFiles) {
                    System.out.println("train file: "
                            + trainFile.getAbsolutePath());
                    PushBackWordStream wordStream = new PushBackWordStream(
                            trainFile.getAbsolutePath(), 100);
                    wordStreamList.add(wordStream);
                }

                CombinedWordInputStream wordStream = new CombinedWordInputStream(
                        wordStreamList);
                System.out.println("building vocab");
                vocab.learnVocabFromTrainStream(wordStream);
                // save vocabulary
                System.out.println("building save vocab");
                vocab.saveVocab(vocabFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ModelParameters aggregate(Integer source, ModelParameters content) {
        System.out.println("old vector: " + modelParams.weights0[2][0] + " "
                + modelParams.weights0[2][1]);
        SkipGramParametersDelta deltaParams = (SkipGramParametersDelta) content;
        System.out.println("delta vector: " + deltaParams.weights0[2][0] + " "
                + deltaParams.weights0[2][1]);
        MathUtils.plusInPlace(modelParams.weights0, deltaParams.weights0,
                1 / 3.0, deltaParams.words_ids0);
        MathUtils.plusInPlace(modelParams.weights1, deltaParams.weights1,
                1 / 3.0, deltaParams.words_ids1);
        wordCount += deltaParams.wordCount;
        modelParams.alpha = starting_alpha
                * (1 - (double) wordCount / (trainWords + 1));
        if (modelParams.alpha < starting_alpha * 0.0001) {
            modelParams.alpha = starting_alpha * 0.0001;
        }
        System.out.println("new vector: " + modelParams.weights0[2][0] + " "
                + modelParams.weights0[2][1]);
        return deltaParams;
    }

    @Override
    public ModelParameters getInitParameters(Integer source) {
        to_update.put(source, new HashSet<Integer>());
        return modelParams;
    }

    @Override
    public ModelParameters getFinalParameters() {
        return modelParams;
    }

    @Override
    public void finalizeWorker(Integer source) {
        //Remove worker
        to_update.remove(source);
    }

}
