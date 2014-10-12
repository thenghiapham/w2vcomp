package demo;

import io.sentence.BasicTreeInputStream;
import io.sentence.TreeInputStream;
import io.word.TreeWordInputStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import common.IOUtils;
import common.LogUtils;

import vocab.Vocab;
import word2vec.Sentence2Vec;
import word2vec.SingleThreadedSentence2Vec;

import demo.TestConstants;

public class SentenceVectorLearning {
    public static void main(String[] args) throws IOException{
//        LogUtils.logToConsole(Level.ALL);
        int hiddenLayerSize = 10;
        int windowSize = 5;
        boolean hierarchialSoftmax = true;
        int negativeSampling = 0;
        double subSampling = 0;
        int phraseLevel = 2;
        boolean allLevel = true;
        String constructionFile = TestConstants.S_CONSTRUCTION_FILE;
        HashMap<String, String> constructionGroups = IOUtils.readConstructionGroup(constructionFile);
        IOUtils.printConstructions(constructionGroups);
        Sentence2Vec sentence2vec = new SingleThreadedSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, phraseLevel, 
//                hierarchialSoftmax, negativeSampling, subSampling, null, phraseLevel,
                allLevel, true, TestConstants.S_MEN_FILE);
        String trainFile = TestConstants.S_TRAIN_FILE;
        String outputFile = TestConstants.S_VECTOR_FILE;
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String logFile = TestConstants.S_LOG_FILE;
        LogUtils.setup(logFile);
        
        System.out.println("Starting training using file " + trainFile);
        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(5);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            WordInputStream wordInputStream = new TreeWordInputStream(new BasicTreeInputStream(trainFile));
            vocab.learnVocabFromTrainStream(wordInputStream);
            wordInputStream.close();
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        sentence2vec.setVocab(vocab);

        sentence2vec.initNetwork();

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            TreeInputStream treeInputStream = new BasicTreeInputStream(trainFile);;
            ArrayList<TreeInputStream> inputStreams = new ArrayList<TreeInputStream>();
            inputStreams.add(treeInputStream);
            sentence2vec.trainModel(inputStreams);
            sentence2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
