package demo;

import io.sentence.BasicTreeInputStream;
import io.sentence.TreeInputStream;
import io.word.TreeWordInputStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import neural.function.ActivationFunction;
import neural.function.IdentityFunction;
//import java.util.logging.Level;

import common.IOUtils;
import common.LogUtils;
import common.correlation.MenCorrelation;
import common.correlation.ParsedPhraseCorrelation;

import vocab.Vocab;
import word2vec.MultiThreadDiagonalSentence2Vec;

import demo.TestConstants;

public class SentenceVectorLearning {
    public static void main(String[] args) throws IOException{
//        LogUtils.logToConsole(Level.ALL);
        int hiddenLayerSize = 300;
        int windowSize = 5;
        boolean hierarchialSoftmax = true;
        int negativeSampling = 0;
        double subSampling = 0;
        int phraseLevel = -1;
        boolean allLevel = true;
        boolean lexical = true;
        String constructionFile = TestConstants.S_CONSTRUCTION_FILE;
        ActivationFunction hiddenActivationFunction = new IdentityFunction();
        HashMap<String, String> constructionGroups = IOUtils.readConstructionGroup(constructionFile);
        MultiThreadDiagonalSentence2Vec sentence2vec = new MultiThreadDiagonalSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
//        SingleThreadedSentence2Vec sentence2vec = new SingleThreadedSentence2Vec(hiddenLayerSize, windowSize, 
//                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
//                allLevel, lexical);
        String trainFile = TestConstants.S_TRAIN_FILE;
        String outputFile = TestConstants.S_VECTOR_FILE;
        String compFile = TestConstants.S_COMPOSITION_FILE;
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
        MenCorrelation men = new MenCorrelation(TestConstants.S_MEN_FILE);
        men.setName("MEN");
        sentence2vec.addMenCorrelation(men);
        
        ParsedPhraseCorrelation sick = new ParsedPhraseCorrelation(TestConstants.S_SICK_FILE);
        sick.setName("SICK");
        sentence2vec.addSentenceCorrelation(sick);
        
//        ArrayList<Tree> validationTrees = IOUtils.readTree(TestConstants.S_VALIDATION_FILE);
//        sentence2vec.setTestTrees(validationTrees);
        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            TreeInputStream treeInputStream = new BasicTreeInputStream(trainFile);;
            ArrayList<TreeInputStream> inputStreams = new ArrayList<TreeInputStream>();
            inputStreams.add(treeInputStream);
            sentence2vec.trainModel(inputStreams);
            sentence2vec.saveVector(outputFile, true);
            sentence2vec.saveCompositionNetwork(compFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
