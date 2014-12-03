package demo;

import io.sentence.BasicTreeInputStream;
import io.sentence.TreeInputStream;
import io.word.CombinedWordInputStream;
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
import word2vec.MTSingleObjectSentence2Vec;
import word2vec.MTWeightedSingleObjectSentence2Vec;
import word2vec.MultiThreadDiagonalSentence2Vec;
//import word2vec.MultiThreadDiagonalSentence2Vec;
import word2vec.MultiThreadSentence2Vec;
import word2vec.MultiThreadWeightedSentence2Vec;
import demo.TestConstants;

public class MultiThreadSentenceVectorLearning {
    public static void main(String[] args) throws IOException{
//        LogUtils.logToConsole(Level.ALL);
        int hiddenLayerSize = 40;
        int windowSize = 5;
        boolean hierarchialSoftmax = true;
        int negativeSampling = 0;
        double subSampling = 0;
        int phraseLevel = 5;
        boolean allLevel = true;
        boolean lexical = true;
        String constructionFile = TestConstants.S_CONSTRUCTION_FILE;
        ActivationFunction hiddenActivationFunction = new IdentityFunction();
        HashMap<String, String> constructionGroups = IOUtils.readConstructionGroup(constructionFile);
        MTWeightedSingleObjectSentence2Vec sentence2vec = new MTWeightedSingleObjectSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
//        MultiThreadWeightedSentence2Vec sentence2vec = new MultiThreadWeightedSentence2Vec(hiddenLayerSize, windowSize, 
//                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
//                allLevel, lexical);
//        MultiThreadDiagonalSentence2Vec sentence2vec = new MultiThreadDiagonalSentence2Vec(hiddenLayerSize, windowSize, 
//                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
//                allLevel, lexical);
//        MultiThreadSentence2Vec sentence2vec = new MultiThreadSentence2Vec(hiddenLayerSize, windowSize, 
//                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
//                allLevel, lexical);
        String trainDirPath = TestConstants.S_TRAIN_DIR;
        String outputFile = TestConstants.S_VECTOR_FILE;
        String compFile = TestConstants.S_COMPOSITION_FILE;
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String logFile = TestConstants.S_LOG_FILE;
        LogUtils.setup(logFile);
        
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        
        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(5);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
            for (File trainFile: trainFiles) {
                WordInputStream wordStream = new TreeWordInputStream(new BasicTreeInputStream(trainFile));
                wordStreamList.add(wordStream);
            }
            CombinedWordInputStream wordInputStream = new CombinedWordInputStream(wordStreamList);
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
            
            ArrayList<TreeInputStream> inputStreams = new ArrayList<TreeInputStream>();
            for (File trainFile: trainFiles) {
                TreeInputStream treeInputStream = new BasicTreeInputStream(trainFile);;
                inputStreams.add(treeInputStream);
            }
            System.out.println(inputStreams.size());
            sentence2vec.trainModel(inputStreams);
            sentence2vec.saveVector(outputFile, true);
            sentence2vec.saveCompositionNetwork(compFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
