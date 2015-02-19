package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.PushBackWordStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vocab.Vocab;
//import word2vec.CBowWord2Vec;
import word2vec.SkipNGramWord2Vec;

import demo.TestConstants;

public class WordVectorLearning {
    public static void main(String[] args) {
//        CBowWord2Vec word2vec = new CBowWord2Vec(200, 5, true, 0, (float) 0);
        //CBowWord2Vec word2vec = new CBowWord2Vec(200, 5, false, 10, (float) 1e-3);
        //SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, true, 0, (float) 1e-3);
        SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, true, 0, (float) 1e-3, TestConstants.S_MEN_FILE);
//        MultiThreadSkipGram word2vec = new MultiThreadSkipGram(200, 5, false, 10, (float) 1e-3, TestConstants.S_MEN_FILE);
        // CBowWord2Vec word2vec = new SimpleWord2Vec(200, 5, false, 10, (float)
        // 0);
        String trainFile = TestConstants.TRAIN_FILE;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        System.out.println("Starting training using file " + trainFile);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(50);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            vocab.learnVocabFromTrainFile(trainFile);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);

//        word2vec.initNetwork(initFile);
        word2vec.initNetwork();

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            SentenceInputStream sentenceInputStream = new PlainSentenceInputStream(
                    new PushBackWordStream(trainFile, 100));
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            inputStreams.add(sentenceInputStream);
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
