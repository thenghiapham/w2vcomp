package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import common.LogUtils;
import common.exception.ValueException;

import vocab.Vocab;
import word2vec.MmSkipNGramWithMappingMaxMargin;
import word2vec.MultiMmSkipNGramWithMappingMaxMargin;
import word2vec.MultiThreadMMSkipgram;
import word2vec.MultiThreadMMSkipgramMaxMargin;
import word2vec.MultiThreadSkipGram;
import word2vec.extra.MultiThreadMMSkipgramMaxMarginBiDirectional;
//import word2vec.MultiThreadSkipGram;
//import word2vec.CBowWord2Vec;

import demo.TestConstants;

public class MultiThreadWordVectorLearning {
    public static void main(String[] args) throws ValueException, IOException {
        
        //MmSkipNGramWithMappingCosine word2vec = new MmSkipNGramWithMappingCosine(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, (float) 1e-3, TestConstants.CCG_MEN_FILE);
        MultiMmSkipNGramWithMappingMaxMargin word2vec = new MultiMmSkipNGramWithMappingMaxMargin(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, (float) 1e-3,TestConstants.CCG_MEN_FILE);
        //MultiThreadMMSkipgramMaxMarginBiDirectional word2vec = new MultiThreadMMSkipgramMaxMarginBiDirectional(TestConstants.wordDimensions, 5, true, 0, TestConstants.negative_samples, (float) 1e-3, TestConstants.CCG_MEN_FILE);
        //MultiThreadMMSkipgramMaxMargin word2vec = new MultiThreadMMSkipgramMaxMargin(TestConstants.wordDimensions, 5, true, 0, TestConstants.negative_samples, (float) 1e-5, TestConstants.CCG_MEN_FILE);
        //MultiThreadMMSkipgram word2vec = new MultiThreadMMSkipgram(TestConstants.wordDimensions, 5, true, 0, TestConstants.negative_samples, (float) 1e-3, TestConstants.CCG_MEN_FILE);
        
        //TODO: Assume that we extend vocabulary with new items
        //TODO: Assume that for every word we have its extended context
        String trainDirPath = TestConstants.TRAIN_DIR;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        String initFile = TestConstants.INITIALIZATION_FILE;
        String logGile = TestConstants.LOG_FILE;
        String mapFile = TestConstants.MAPPING_FUNCTION;
        String modelFile = TestConstants.MODEL_FILE;
        String projInitFile = TestConstants.IMAGE_INITIALIZATION_FILE;

        System.out.println("Welcome!---->"+TestConstants.VECTOR_FILE);
        LogUtils.setup(logGile);
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        
        boolean learnVocab = !(new File(vocabFile)).exists();
        
        
        Vocab vocab = new Vocab(5); //wiki 50, enwik9 5
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
            for (File trainFile: trainFiles) {
                PushBackWordStream wordStream = new PushBackWordStream(trainFile.getAbsolutePath(), 100);
                wordStreamList.add(wordStream);
            }
            
            CombinedWordInputStream wordStream = new CombinedWordInputStream(wordStreamList);
            vocab.learnVocabFromTrainStream(wordStream);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);
        word2vec.initNetwork(initFile,projInitFile);
        word2vec.saveMappingFunction(mapFile, false);
        word2vec.saveNetwork(modelFile, true);
        
        word2vec.initImages(TestConstants.VISION_FILE,false);
        
        
        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            for (File trainFile: trainFiles) {
                SentenceInputStream sentenceInputStream = new PlainSentenceInputStream(
                        new PushBackWordStream(trainFile.getAbsolutePath(), 100));
                inputStreams.add(sentenceInputStream);
            }
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
            word2vec.saveMappingFunction(mapFile, false);
            word2vec.saveNetwork(modelFile, true);
        } catch (IOException e) {
            System.exit(1);
        }
       
        double [] cors = word2vec.getCors();
        System.out.println("Printing pearson "+cors[0]);
        System.out.println("Printing spearman "+cors[1]);
    }
}
