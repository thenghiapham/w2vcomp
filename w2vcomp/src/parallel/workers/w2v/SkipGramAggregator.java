package parallel.workers.w2v;


import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import common.MathUtils;

import demo.TestConstants;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterAggregator;
import vocab.Vocab;
import word2vec.AbstractWord2Vec;

public class SkipGramAggregator implements ParameterAggregator{
    protected double           alpha;
    protected double           starting_alpha;
    protected Vocab            vocab;

    protected long             wordCount;
    protected long             trainWords;

    double[][]                 weights0, weights1;
    SkipGramParameters         modelParams;
    Random rand = new Random();
    public SkipGramAggregator() {
        starting_alpha = AbstractWord2Vec.DEFAULT_STARTING_ALPHA;
        alpha = starting_alpha;
        vocab = new Vocab(RunningConstant.MIN_FREQUENCY);
        buildVocab(TestConstants.S_VOCABULARY_FILE);
        trainWords = vocab.getTrainWords();
        
        int vocabSize = vocab.getVocabSize();
        int projectionLayerSize = RunningConstant.VECTOR_SIZE;
        weights0 = new double[vocabSize][projectionLayerSize];
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
        vocab.assignCode();
        modelParams = new SkipGramParameters(alpha, wordCount, weights0, weights1);
    }
    
    public void buildVocab(String vocabFile) {
        boolean learnVocab = !(new File(vocabFile)).exists();
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            try {
                File trainDir = new File(TestConstants.S_TRAIN_DIR);
                System.out.println("train dir: " + trainDir.getAbsolutePath());
                File[] trainFiles = trainDir.listFiles();
                ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
                for (File trainFile: trainFiles) {
                    System.out.println("train file: " + trainFile.getAbsolutePath());
                    PushBackWordStream wordStream = new PushBackWordStream(trainFile.getAbsolutePath(), 100);
                    wordStreamList.add(wordStream);
                }
                
                CombinedWordInputStream wordStream = new CombinedWordInputStream(wordStreamList);
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
    public ModelParameters aggregate(ModelParameters content) {
        // TODO Auto-generated method stub
        SkipGramParameters deltaParams = (SkipGramParameters) content;
        System.out.println("delta vector: " + deltaParams.weights0[2][0] + " " + deltaParams.weights0[2][1]);
        MathUtils.plusInPlace(weights0, deltaParams.weights0);
        MathUtils.plusInPlace(weights1, deltaParams.weights1);
        wordCount += deltaParams.wordCount;
        alpha = starting_alpha
                * (1 - (double) wordCount / (trainWords + 1));
        if (alpha < starting_alpha * 0.0001) {
            alpha = starting_alpha * 0.0001;
        }
        System.out.println("vector: " + weights0[2][0] + " " + weights0[2][1]);
        modelParams = new SkipGramParameters(alpha, wordCount, weights0, weights1);
        return modelParams;
    }

    @Override
    public ModelParameters getInitParameters() {
        // TODO Auto-generated method stub
        return modelParams;
    }
    
    @Override
    public ModelParameters getFinalParameters() {
        // TODO Auto-generated method stub
        return modelParams;
    }

}
