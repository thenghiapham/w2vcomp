package demo;

import io.sentence.BasicTreeInputStream;
import io.sentence.ParseSentenceInputStream;
import io.sentence.SentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.TreeWordInputStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import common.LogUtils;
import vocab.Vocab;
import word2vec.MultiThreadContextSkipgram;
import demo.TestConstants;


/*
 * Learning normal skipgram and Cbow but the input is the parsed corpus
 * Need to preprocess the input into plain text
 * 
 * This class is similar to WordVectorLearning2
 * The different is that most of the parameters are in the config file
 */
public class WordVectorLearning3 {
    public static void main(String[] args) throws IOException{
        int hiddenSize = Integer.parseInt(args[0]);
        int windowSize = Integer.parseInt(args[1]);
        boolean hierarchicalSoftmax = false;
        int negativeSamples = 10;
        MultiThreadContextSkipgram word2vec = new MultiThreadContextSkipgram(hiddenSize, windowSize, hierarchicalSoftmax, negativeSamples, 1e-3, TestConstants.S_MEN_FILE);
        String trainDirPath = TestConstants.S_TRAIN_DIR;
        String outputFile = TestConstants.S_WORD_VECTOR_FILE.replace("skip", "cskip").replaceAll("size", "" + hiddenSize).replace("ws", "" + windowSize);
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String modelFile = TestConstants.S_WORD_MODEL_FILE.replace("skip", "cskip").replaceAll("size", "" + hiddenSize).replace("ws", "" + windowSize);
        System.out.println("Starting training using files in " + trainDirPath);

        boolean learnVocab = !(new File(vocabFile)).exists();
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        String logFile = TestConstants.S_WORD_LOG_FILE.replace("skip", "cskip").replaceAll("size", "" + hiddenSize).replace("ws", "" + windowSize);
        LogUtils.setup(logFile);
        
        Vocab vocab = new Vocab(TestConstants.S_MIN_FREQUENCY);
        
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
            for (File trainFile: trainFiles) {
                TreeWordInputStream wordStream = new TreeWordInputStream(new BasicTreeInputStream(trainFile));
                wordStreamList.add(wordStream);
            }
          
            CombinedWordInputStream wordStream = new CombinedWordInputStream(wordStreamList);
            vocab.learnVocabFromTrainStream(wordStream);
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);

        word2vec.initNetwork();

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            for (File trainFile: trainFiles) {
                SentenceInputStream sentenceInputStream = new ParseSentenceInputStream(new BasicTreeInputStream(trainFile));
                inputStreams.add(sentenceInputStream);
            }
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
            word2vec.saveNetwork(modelFile, true);
        } catch (IOException e) {
            System.exit(1);
        }


    }
}
