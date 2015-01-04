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
import neural.function.Tanh;
//import java.util.logging.Level;

import common.IOUtils;
import common.LogUtils;
import common.correlation.MenCorrelation;
import common.correlation.ParsedPhraseCorrelation;
import vocab.Vocab;
import word2vec.MTIncrementalDiagonalSentence2Vec;
import word2vec.MTIncrementalSentence2Vec;
import word2vec.MTIncrementalWeightedSentence2Vec;
import word2vec.MTNewDiagonalSOSentence2Vec;
import demo.TestConstants;

public class MultiThreadSentenceVectorLearning {
    public static void main(String[] args) throws IOException{
//        LogUtils.logToConsole(Level.ALL);
        int hiddenLayerSize = Integer.parseInt(args[0]);
        int windowSize = Integer.parseInt(args[1]);
        int incrementalStep = Integer.parseInt(args[2]);
        boolean hierarchialSoftmax = false;
        int negativeSampling = 10;
        double subSampling = 1e-3;
        int phraseLevel = 6;
        boolean allLevel = true;
        boolean lexical = false;
        String constructionFile = TestConstants.S_CONSTRUCTION_FILE;
        ActivationFunction hiddenActivationFunction = new IdentityFunction();
//        ActivationFunction hiddenActivationFunction = new Tanh();
//        HashMap<String, String> constructionGroups = new HashMap<String, String>();
        HashMap<String, String> constructionGroups = IOUtils.readConstructionGroup(constructionFile);
//        MTWeightedSingleObjectSentence2Vec sentence2vec = new MTWeightedSingleObjectSentence2Vec(hiddenLayerSize, windowSize, 
//                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
//                allLevel, lexical);
//        MTIncrementalWeightedSentence2Vec sentence2vec = new MTIncrementalWeightedSentence2Vec(hiddenLayerSize, windowSize, 
//                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
//                allLevel, lexical, incrementalStep);
//        MTIncrementalSentence2Vec sentence2vec = new MTIncrementalSentence2Vec(hiddenLayerSize, windowSize, 
//                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
//                allLevel, lexical);
        MTIncrementalDiagonalSentence2Vec sentence2vec = new MTIncrementalDiagonalSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical, incrementalStep);
        
        String trainDirPath = TestConstants.S_TRAIN_DIR;
        String outputFile = TestConstants.S_VECTOR_FILE.replace("istep", ""+incrementalStep).replace("size", ""+hiddenLayerSize).replace("window", ""+windowSize);
        String compFile = TestConstants.S_COMPOSITION_FILE.replace("istep", ""+incrementalStep).replace("size", ""+hiddenLayerSize).replace("window", ""+windowSize);
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String logFile = TestConstants.S_LOG_FILE.replace("istep", ""+incrementalStep).replace("size", ""+hiddenLayerSize).replace("window", ""+windowSize);;
//        String skipMdlFile = TestConstants.S_INITIALIZATION_FILE.replace(".mdl", "" + hiddenLayerSize + ".mdl");
        LogUtils.setup(logFile);
        
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        
        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(TestConstants.S_MIN_FREQUENCY);
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
//        sentence2vec.initNetwork(skipMdlFile);
        MenCorrelation men = new MenCorrelation(TestConstants.S_MEN_FILE);
        men.setName("MEN");
        sentence2vec.addMenCorrelation(men);
        
        ParsedPhraseCorrelation sick = new ParsedPhraseCorrelation(TestConstants.S_SICK_FILE);
        sick.setName("SICK");
        sentence2vec.addSentenceCorrelation(sick);
        
//        ArrayList<Tree> validationTrees = IOUtils.readTree(TestConstants.S_VALIDATION_FILE);
//        sentence2vec.setTestTrees(validationTrees);
        // single threaded instead of multithreading
//        System.out.println("Start training");
//        try {
//            
//            ArrayList<TreeInputStream> inputStreams = new ArrayList<TreeInputStream>();
//            for (File trainFile: trainFiles) {
//                TreeInputStream treeInputStream = new BasicTreeInputStream(trainFile);;
//                inputStreams.add(treeInputStream);
//            }
//            System.out.println(inputStreams.size());
//            sentence2vec.trainModel(inputStreams);
////            sentence2vec.saveVector(outputFile, true);
////            sentence2vec.saveCompositionNetwork(compFile, true);
//        } catch (IOException e) {
//            System.exit(1);
//        }
        
        System.out.println("train again");
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
