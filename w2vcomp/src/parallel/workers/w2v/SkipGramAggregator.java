package parallel.workers.w2v;


import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    public SkipGramAggregator() {
        starting_alpha = AbstractWord2Vec.DEFAULT_STARTING_ALPHA;
        alpha = starting_alpha;
        Vocab vocab = new Vocab(RunningConstant.MIN_FREQUENCY);
        buildVocab(TestConstants.S_VOCABULARY_FILE);
        trainWords = vocab.getTrainWords();
        
        int vocabSize = vocab.getVocabSize();
        int projectionLayerSize = RunningConstant.VECTOR_SIZE;
        weights0 = new double[vocabSize][projectionLayerSize];
        if (RunningConstant.HIERARCHICAL_SOFTMAX) {
            weights1 = new double[vocabSize - 1][projectionLayerSize];
        } else if (RunningConstant.NEGATIVE_SAMPLES > 0) {
            weights1 = new double[vocabSize][projectionLayerSize];
        }
        vocab.assignCode();
    }
    
    public void buildVocab(String vocabFile) {
        boolean learnVocab = !(new File(vocabFile)).exists();
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            try {
                File trainDir = new File(TestConstants.S_TRAIN_DIR);
                File[] trainFiles = trainDir.listFiles();
                ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
                for (File trainFile: trainFiles) {
                    PushBackWordStream wordStream = new PushBackWordStream(trainFile.getAbsolutePath(), 100);
                    wordStreamList.add(wordStream);
                }
                
                CombinedWordInputStream wordStream = new CombinedWordInputStream(wordStreamList);
                vocab.learnVocabFromTrainStream(wordStream);
                // save vocabulary
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
        MathUtils.plusInPlace(weights0, deltaParams.weights0);
        MathUtils.plusInPlace(weights1, deltaParams.weights1);
        wordCount += deltaParams.wordCount;
        alpha = starting_alpha
                * (1 - (double) wordCount / (trainWords + 1));
        if (alpha < starting_alpha * 0.0001) {
            alpha = starting_alpha * 0.0001;
        }
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
