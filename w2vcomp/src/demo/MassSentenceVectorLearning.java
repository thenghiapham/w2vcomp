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
import common.IOUtils;
import common.LogUtils;
import common.correlation.MenCorrelation;
import common.correlation.ParsedPhraseCorrelation;
import vocab.Vocab;
import word2vec.MTDiagonalSingleObjectSentence2Vec;
import word2vec.MTSingleObjectSentence2Vec;
import word2vec.MTWeightedSingleObjectSentence2Vec;
//import word2vec.MultiThreadDiagonalSentence2Vec;
//import word2vec.MultiThreadSentence2Vec;
//import word2vec.MultiThreadWeightedSentence2Vec;
import demo.TestConstants;

public class MassSentenceVectorLearning {
    public static void main(String[] args) throws IOException{
        String hiddenLayerString = args[0];
        String compType = args[1];
        String levelString = args[2];
        String allLevelString = args[3];
        String lexicalString = args[4];
        
        String outSuffix = "so_" + compType + hiddenLayerString + "_" + levelString + "_" 
                + allLevelString.charAt(0) + lexicalString.charAt(0);
        
        String constructionFile = TestConstants.S_CONSTRUCTION_FILE;
        
        int windowSize = 5;
        boolean hierarchialSoftmax = true;
        int negativeSampling = 0;
        double subSampling = 0;
        
        int hiddenLayerSize = Integer.parseInt(hiddenLayerString);
        int phraseLevel = Integer.parseInt(levelString);
        boolean allLevel = Boolean.parseBoolean(allLevelString);
        boolean lexical = Boolean.parseBoolean(lexicalString);
        
        ActivationFunction hiddenActivationFunction = new IdentityFunction();
        HashMap<String, String> constructionGroups = IOUtils.readConstructionGroup(constructionFile);
        MTSingleObjectSentence2Vec sentence2vec;
        switch (compType) {
        case "w":
            sentence2vec = new MTWeightedSingleObjectSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
            break;
        case "d":
            sentence2vec = new MTDiagonalSingleObjectSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
            break;
        case "f":
            sentence2vec = new MTSingleObjectSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
            break;
        default:
            sentence2vec = new MTWeightedSingleObjectSentence2Vec(hiddenLayerSize, windowSize, 
                    hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                    allLevel, lexical);
            break;
        }
                
        
        String outputFile = TestConstants.S_VECTOR_FILE.replace(".bin", outSuffix + ".bin");
        String compFile = TestConstants.S_COMPOSITION_FILE.replace(".cmp", outSuffix + ".cmp");
        String vocabFile = TestConstants.S_VOCABULARY_FILE;//.replace(".voc", outSuffix + ".voc");
        LogUtils.setup(TestConstants.S_LOG_FILE.replace(".log", outSuffix + ".log"));
        
        String trainDirPath = TestConstants.S_TRAIN_DIR;
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
        MenCorrelation men = new MenCorrelation(TestConstants.S_MEN_FILE);
        men.setName("MEN");
        sentence2vec.addMenCorrelation(men);
        
        ParsedPhraseCorrelation sick = new ParsedPhraseCorrelation(TestConstants.S_SICK_FILE);
        sick.setName("SICK");
        sentence2vec.addSentenceCorrelation(sick);
        
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
