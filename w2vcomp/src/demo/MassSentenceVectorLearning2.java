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
import demo.TestConstants;

public class MassSentenceVectorLearning2 {
    public static void main(String[] args) throws IOException{
//        LogUtils.logToConsole(Level.ALL);
        int hiddenLayerSize = Integer.parseInt(args[0]);
        int windowSize = 5;
        boolean hierarchialSoftmax = true;
        int negativeSampling = 0;
        double subSampling = 1e-3;
        int phraseLevel = 6;
        boolean allLevel = true;
        boolean lexical = true;
        String constructionFile = TestConstants.S_CONSTRUCTION_FILE;
        String activation = args[1];
        ActivationFunction hiddenActivationFunction = new IdentityFunction();
        if (activation.equals("tanh")) {
            hiddenActivationFunction = new Tanh();
        } else {
            activation = "identity";
        }
        String compType = args[2];
        MTIncrementalSentence2Vec sentence2vec;
        HashMap<String, String> constructionGroups = IOUtils.readConstructionGroup(constructionFile);
        switch (compType) {
        case "w":
            sentence2vec = new MTIncrementalWeightedSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
            break;
        case "d":
            sentence2vec = new MTIncrementalDiagonalSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
            break;
        case "f":
            sentence2vec = new MTIncrementalSentence2Vec(hiddenLayerSize, windowSize, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                allLevel, lexical);
            break;
        default:
            sentence2vec = new MTIncrementalWeightedSentence2Vec(hiddenLayerSize, windowSize, 
                    hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                    allLevel, lexical);
            break;
        }
        String suffix = "" + hiddenLayerSize + activation.charAt(0) + "s";
        String infix = compType + "wiki";
        String trainDirPath = TestConstants.S_TRAIN_DIR;
        String outputFile = TestConstants.S_VECTOR_FILE.replaceAll("wiki", infix).replaceAll("size", suffix);;
        String compFile = TestConstants.S_COMPOSITION_FILE.replaceAll("wiki", infix).replaceAll("size", suffix);
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String logFile = TestConstants.S_LOG_FILE.replaceAll("wiki", infix).replaceAll("size", suffix);;
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
        
        
        System.out.println("train");
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
