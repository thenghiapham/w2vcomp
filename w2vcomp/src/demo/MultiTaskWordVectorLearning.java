package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;





import java.util.HashSet;

import common.IOUtils;
import common.wordnet.WordNetAdj;
import common.wordnet.WordNetNoun;
import vocab.Vocab;
import word2vec.MultiThreadAlterSkipGram;
import word2vec.MultiThreadSkipGram;
import word2vec.multitask.AntonymWord2Vec;
//import word2vec.CBowWord2Vec;
//import word2vec.SkipNGramWord2Vec;
import demo.TestConstants;

public class MultiTaskWordVectorLearning {
    public static void main(String[] args) throws IOException{
        
        boolean softmax = false;
        int negativeSamples = 10;
        MultiThreadSkipGram word2vec = new MultiThreadSkipGram(300, 5, softmax, negativeSamples, (float) 1e-3, TestConstants.MEN_FILE);
//        MultiThreadAlterSkipGram word2vec = new MultiThreadAlterSkipGram(300, 5, softmax, negativeSamples, (float) 1e-3, TestConstants.MEN_FILE);
////        WordNetAdj wordNetAdj = new WordNetAdj("/home/nghia/Downloads/dict/data.adj");
////        AntonymWord2Vec word2vec = new AntonymWord2Vec(300, 5, softmax, negativeSamples, 5, wordNetAdj, (float) 1e-3, TestConstants.MEN_FILE);
//        WordNetNoun wordNetNoun = new WordNetNoun("/home/nghia/Downloads/dict/data.noun");
//        AntonymWord2Vec word2vec = new AntonymWord2Vec(300, 5, softmax, negativeSamples, 5,  (float) 1e-3, TestConstants.MEN_FILE);
////        ArrayList<String> forbiddenWords = IOUtils.readFile("/home/nghia/test.word.txt");
//        HashSet<String> forbiddenSet = new HashSet<String>();
//        String outputFile = TestConstants.VECTOR_FILE.replaceAll(".bin", "_anto.bin");
////        HashSet<String> forbiddenSet = new HashSet<String>(forbiddenWords);
////        String outputFile = TestConstants.VECTOR_FILE.replaceAll(".bin", "_anto_train.bin");
//        word2vec.setForbiddenWords(forbiddenSet);
//        word2vec.setWordNetNoun(wordNetNoun);
        
        
        String outputFile = TestConstants.VECTOR_FILE.replaceAll(".bin", "_old.bin");
        
//        String outputFile = TestConstants.VECTOR_FILE;
        String trainDirPath = TestConstants.TRAIN_DIR;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        System.out.println("Starting training using dir " + trainDirPath);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(50);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
            for (File trainFile: trainFiles) {
                WordInputStream wordStream = new PushBackWordStream(trainFile.getAbsolutePath(), 200);
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
                SentenceInputStream sentenceInputStream = new PlainSentenceInputStream(
                    new PushBackWordStream(trainFile.getAbsolutePath(), 200));
                inputStreams.add(sentenceInputStream);
            }
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
