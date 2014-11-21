package demo;

import io.sentence.PlainSentenceInputStream;
import io.sentence.SentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vocab.Vocab;
import word2vec.MultiThreadSkipGram;
//import word2vec.CBowWord2Vec;

import demo.TestConstants;

public class MTWordVectorLearning {
    public static void main(String[] args) throws IOException{
//        CBowWord2Vec word2vec = new CBowWord2Vec(200, 5, true, 0, (float) 0);
        //CBowWord2Vec word2vec = new CBowWord2Vec(200, 5, false, 10, (float) 1e-3);
        //SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, true, 0, (float) 1e-3);
//        SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, false, 10, (float) 1e-3, TestConstants.S_MEN_FILE);
        MultiThreadSkipGram word2vec = new MultiThreadSkipGram(100, 5, true, 0, (float) 0, TestConstants.S_MEN_FILE);
        // CBowWord2Vec word2vec = new SimpleWord2Vec(200, 5, false, 10, (float)
        // 0);
        String trainDirPath = TestConstants.TRAIN_DIR;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        System.out.println("Starting training using files in " + trainDirPath);

        boolean learnVocab = !(new File(vocabFile)).exists();
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        Vocab vocab = new Vocab(50);
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

//        word2vec.initNetwork(initFile);
        word2vec.initNetwork();

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            for (File trainFile: trainFiles) {
                
                System.out.println(trainFile.getAbsolutePath());
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
