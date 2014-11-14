package demo;

import io.sentence.BasicTreeInputStream;
import io.sentence.ParseSentenceInputStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import common.LogUtils;

import vocab.Vocab;
import word2vec.ParseTreeWord2Vec;
//import word2vec.CBowWord2Vec;

import demo.TestConstants;

public class WordVectorLearning2 {
    public static void main(String[] args) throws IOException{
//        CBowWord2Vec word2vec = new CBowWord2Vec(200, 5, true, 0, (float) 0);
        //CBowWord2Vec word2vec = new CBowWord2Vec(200, 5, false, 10, (float) 1e-3);
        //SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, true, 0, (float) 1e-3);
//        SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, false, 10, (float) 1e-3);
        ParseTreeWord2Vec word2vec = new ParseTreeWord2Vec(300, 5, true, 0, 0, TestConstants.S_MEN_FILE);
        // CBowWord2Vec word2vec = new SimpleWord2Vec(200, 5, false, 10, (float)
        // 0);
        String trainFile = TestConstants.S_TRAIN_FILE;
        String outputFile = TestConstants.S_VECTOR_FILE;
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String logFile = TestConstants.S_LOG_FILE;
        String initFile = TestConstants.S_INITIALIZATION_FILE;
        System.out.println("Starting training using file " + trainFile);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(5);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            vocab.learnVocabFromTrainFile(trainFile);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);

        word2vec.initNetwork(initFile);
        LogUtils.setup(logFile);
        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            SentenceInputStream sentenceInputStream = new ParseSentenceInputStream(
                    new BasicTreeInputStream(trainFile));
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            inputStreams.add(sentenceInputStream);
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
