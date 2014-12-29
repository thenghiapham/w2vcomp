package parallel.workers.w2v;

import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import common.IOUtils;
import common.MathUtils;
import common.exception.UnimplementedException;
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
    Map<Integer, Set<Integer>> to_update0, to_update1;
    // Model parameters
    SkipGramParameters         modelParams;
    
    public double[][][] loadModel(String modelFile) throws IOException {
        double[][][] model = new double[2][][];
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(modelFile));
        model[0] = IOUtils.readMatrix(inputStream, true);
        model[1] = IOUtils.readMatrix(inputStream, true);
        inputStream.close();
        return model;
    }

    public SkipGramAggregator(int iteration, int iterationNum, String modelFile) {
        System.out.println("iteration: " + (iteration + 1) + " in " + iterationNum);
        starting_alpha = 0.025;
        
        Vocab vocab = new Vocab(RunningConstant.MIN_FREQUENCY);
        buildVocab(vocab, TestConstants.S_VOCABULARY_FILE);
        trainWords = vocab.getTrainWords();
        
        wordCount = trainWords * iteration;
        trainWords = trainWords * iterationNum;

        int vocabSize = vocab.getVocabSize();
        int projectionLayerSize = RunningConstant.VECTOR_SIZE;
        double[][] weights0 = null;
        double[][] weights1 = null;
        if (iterationNum == 0) {
            weights0 = new double[vocabSize][projectionLayerSize];
            Random rand = new Random();
            for (int i = 0; i < vocab.getVocabSize(); i++) {
                for (int j = 0; j < projectionLayerSize; j++) {
                    weights0[i][j] = (double) (rand.nextFloat() - 0.5)
                            / projectionLayerSize;
                }
            }
            if (RunningConstant.HIERARCHICAL_SOFTMAX) {
                weights1 = new double[vocabSize - 1][projectionLayerSize];
            } else if (RunningConstant.NEGATIVE_SAMPLES > 0) {
                weights1 = new double[vocabSize][projectionLayerSize];
            }
        } else {
            try {
                double[][][] model = loadModel(modelFile);
                weights0 = model[0];
                weights1 = model[1];
            } catch (IOException e) {
                e.printStackTrace();
                throw new UnimplementedException("cannot load model");
            }
        }
        vocab.assignCode();
        // Since it contains all the words in the vocabulary, we don't specify
        // the list of present words
        modelParams = new SkipGramParameters(starting_alpha, weights0, weights1);
        to_update0 = new HashMap<Integer, Set<Integer>>();
        to_update1 = new HashMap<Integer, Set<Integer>>();
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
        
        SkipGramParametersDelta deltaParams = (SkipGramParametersDelta) content;
        System.out.println("old vector: " + modelParams.getWeights0()[deltaParams.getWordsIds0().get(0)][0] + " "
                + modelParams.getWeights0()[deltaParams.getWordsIds0().get(0)][1]);
        System.out.println("delta vector: " + deltaParams.getWeights0()[0][0] + " "
                + deltaParams.getWeights0()[0][1]);
        MathUtils.plusInPlace(modelParams.getWeights0(), deltaParams.getWeights0(),
                1 / 3.0, deltaParams.getWordsIds0());
        MathUtils.plusInPlace(modelParams.getWeights1(), deltaParams.getWeights1(),
                1 / 3.0, deltaParams.getWordsIds1());
        wordCount += deltaParams.wordCount;
        modelParams.setAlpha(starting_alpha
                * (1 - (double) wordCount / (trainWords + 1)));
        if (modelParams.getAlpha() < starting_alpha * 0.0001) {
            modelParams.setAlpha(starting_alpha * 0.0001);
        }
        
        System.out.println("new vector: " + modelParams.getWeights0()[deltaParams.getWordsIds0().get(0)][0] + " "
                + modelParams.getWeights0()[deltaParams.getWordsIds0().get(0)][1]);
        
        //Annotate for all workers that these rows have been modified
        for(Map.Entry<Integer, Set<Integer>> worker_to_update: to_update0.entrySet()){
            worker_to_update.getValue().addAll(deltaParams.getWordsIds0());
        }
        for(Map.Entry<Integer, Set<Integer>> worker_to_update: to_update1.entrySet()){
            worker_to_update.getValue().addAll(deltaParams.getWordsIds1());
        }
        //Prepare parameters to send back
        SkipGramParametersSubset newParams = modelParams.getSubset(to_update0.get(source), to_update1.get(source));

        //Reset the modified rows record for this worker since we are sending them now
        to_update0.get(source).clear();
        to_update1.get(source).clear();
        
        return newParams;
    }

    @Override
    public ModelParameters getInitParameters(Integer source) {
        to_update0.put(source, new HashSet<Integer>());
        to_update1.put(source, new HashSet<Integer>());
        return modelParams;
    }

    @Override
    public void finalize() {
        SkipGramFinalizer finalizer = new SkipGramFinalizer();
        finalizer.finish(modelParams);
    }

    @Override
    public void finalizeWorker(Integer source) {
        //Remove worker
        to_update0.remove(source);
        to_update1.remove(source);
    }

}
