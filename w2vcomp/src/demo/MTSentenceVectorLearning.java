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
import word2vec.MTIncrementalNewDiagonalSentence2Vec;
import word2vec.MTIncrementalRawAddSentence2Vec;
import word2vec.MTIncrementalSentence2Vec;
import word2vec.MTIncrementalWeightedSentence2Vec;
import word2vec.Sentence2Vec;

public class MTSentenceVectorLearning {
    public static void main(String[] args) throws IOException{
        String configFile = args[0];
        int hiddenLayerSize = Integer.parseInt(args[1]);
        int windowSize = Integer.parseInt(args[2]);
        int incrementalStep = Integer.parseInt(args[3]);
        String type = args[4];
        String function = args[5];
        
        boolean hierarchialSoftmax = false;
        int negativeSampling = 10;
        double subSampling = 1e-3;
        int phraseLevel = 6;
        boolean allLevel = true;
        boolean lexical = false;
        
        W2vProperties properties = new W2vProperties(configFile);
//      HashMap<String, String> constructionGroups = new HashMap<String, String>();
        HashMap<String, String> constructionGroups = IOUtils.readConstructionGroup(properties.getProperty("ConstructionFile"));
        
        ActivationFunction hiddenActivationFunction = null;
        if (function.equals("t")) {
            hiddenActivationFunction = new Tanh();
        } else {
            function = "i";
            hiddenActivationFunction = new IdentityFunction();
        }

        Sentence2Vec sentence2vec = null;
        switch (type) {
        case "a":
            sentence2vec = new MTIncrementalRawAddSentence2Vec(hiddenLayerSize, windowSize, 
                    hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                    allLevel, lexical, incrementalStep);
            break;
        case "w":
            sentence2vec = new MTIncrementalWeightedSentence2Vec(hiddenLayerSize, windowSize, 
                  hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                  allLevel, lexical, incrementalStep);
            break;
        case "d":
            sentence2vec = new MTIncrementalDiagonalSentence2Vec(hiddenLayerSize, windowSize, 
                    hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                    allLevel, lexical, incrementalStep);
            break;
        case "nd":
            sentence2vec = new MTIncrementalNewDiagonalSentence2Vec(hiddenLayerSize, windowSize, 
                    hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                    allLevel, lexical, incrementalStep);
        case "f":
            sentence2vec = new MTIncrementalSentence2Vec(hiddenLayerSize, windowSize, 
                  hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, hiddenActivationFunction, phraseLevel, 
                  allLevel, lexical, incrementalStep);
            break;
        default:
            System.out.println("Invalid choice");
            System.exit(0);
            break;
        }
        
        String trainDirPath = properties.getProperty("STrainDir");
        String outputFile = properties.getProperty("SOutputFile").replace("istep", ""+incrementalStep)
                .replace("size", ""+hiddenLayerSize)
                .replace("window", ""+windowSize).replace("function", function)
                .replace("type", type);
        
        String compFile = outputFile.replace(".bin",".cmp");
        String vocabFile = properties.getProperty("VocabFile");
        String logFile = properties.getProperty("SLogFile").replace("istep", ""+incrementalStep)
                .replace("size", ""+hiddenLayerSize)
                .replace("window", ""+windowSize).replace("function", function)
                .replace("type", type).replace(".bin", ".log");;
        LogUtils.setup(logFile);
        
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        
        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(new Integer(properties.getProperty("MinFrequency")));
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
        MenCorrelation men = new MenCorrelation(properties.getProperty("MenFile"));
        men.setName("MEN");
        
        
        ParsedPhraseCorrelation sick = new ParsedPhraseCorrelation(properties.getProperty("SickFile"));
        sick.setName("SICK");
        
        if (type.equals("a")) {
            ((MTIncrementalRawAddSentence2Vec) sentence2vec).addMenCorrelation(men);
            ((MTIncrementalRawAddSentence2Vec) sentence2vec).addSentenceCorrelation(sick);
        } else {
            ((MTIncrementalSentence2Vec) sentence2vec).addMenCorrelation(men);
            ((MTIncrementalSentence2Vec) sentence2vec).addSentenceCorrelation(sick);
        }
        
        
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
