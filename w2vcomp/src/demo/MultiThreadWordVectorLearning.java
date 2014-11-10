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
import word2vec.MultiThreadMMSkipgram;
//import word2vec.MultiThreadSkipGram;
//import word2vec.CBowWord2Vec;

import demo.TestConstants;

public class MultiThreadWordVectorLearning {
    public static void main(String[] args) throws ValueException, IOException {
//        MmSkipNGramWithMappingCosine word2vec = new MmSkipNGramWithMappingCosine(300, 5, true, 0,1, (float) 1e-3, TestConstants.CCG_MEN_FILE);
        MultiThreadMMSkipgram word2vec = new MultiThreadMMSkipgram(300, 5, true, 0,10, (float) 1e-3, TestConstants.MEN_FILE);

        //MmSkipNGramWithMappingDot word2vec = new MmSkipNGramWithMappingDot(300, 5, true, 0, 1, (float) 1e-3, TestConstants.CCG_MEN_FILE);
        //MMSkipNgramWord2Vec word2vec = new MMSkipNgramWord2Vec(300, 5, true, 0, 20, (float) 1e-3, TestConstants.CCG_MEN_FILE);
        
        
        //TODO: Assume that we extend vocabulary with new items
        //TODO: Assume that for every word we have its extended context
        String trainDirPath = TestConstants.TRAIN_DIR;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        String initFile = TestConstants.INITIALIZATION_FILE;
        String logGile = TestConstants.LOG_FILE;
        String mapFile = TestConstants.MAPPING_FUNCTION;
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
        word2vec.initNetwork();
//        word2vec.initNetwork(initFile);
        //word2vec.saveMappingFunction(mapFile, false);
        
        word2vec.initImages(TestConstants.VISION_FILE,true);
        
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
        } catch (IOException e) {
            System.exit(1);
        }
       
    }
}
