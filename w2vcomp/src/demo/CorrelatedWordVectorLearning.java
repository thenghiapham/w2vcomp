package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nu.xom.jaxen.function.NameFunction;
import vocab.Vocab;
import word2vec.MultiThreadSkipGram;
import word2vec.MultiThreadWord2Vec;
import word2vec.multitask.CorrelationSkipGram;

public class CorrelatedWordVectorLearning {
    public static void main(String[] args) throws IOException{
        
        
        MultiThreadWord2Vec word2vec = null;
        String configFile = args[0];
        int size = Integer.parseInt(args[1]);
        int type = Integer.parseInt(args[2]);
        
//        String forbiddenWordFile = null;
//        if (args.length == 7) {
//            forbiddenWordFile = args[6];
//        }
        W2vProperties properties = new W2vProperties(configFile);
        boolean softmax = Boolean.parseBoolean(properties.getProperty("HierarchialSoftmax"));
        int negativeSamples = Integer.parseInt(properties.getProperty("NegativeSampling"));
        double subSampling = Double.parseDouble(properties.getProperty("SubSampling"));
        String trainDirPath = properties.getProperty("TrainDir");
        String outputFile = properties.getProperty("WordVectorFile");
        String vocabFile = properties.getProperty("VocabFile");
        String menFile = properties.getProperty("MenFile");
        String simFile = properties.getProperty("SimlexFile");
        String imageFile = properties.getProperty("ImageFile");
        outputFile = outputFile.replaceAll(".bin", "_" + size + ".bin");
        switch (type) {
        case 0:
            word2vec = new MultiThreadSkipGram(size, 5, softmax, negativeSamples, subSampling, simFile);
            break;
        case 1:
            word2vec = new CorrelationSkipGram(size, 5, softmax, negativeSamples, subSampling, simFile);
            outputFile = outputFile.replaceAll(".bin", "_cor.bin");
            break;
        }
        
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        System.out.println("Starting training using dir " + trainDirPath);
        System.out.println("Output file: " + outputFile);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(Integer.parseInt(properties.getProperty("MinFrequency")));
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
        word2vec.initNetwork();
        
        if (type==1) {
//            ((CorrelationSkipGram) word2vec).addTrainedCorrelation(menFile,"men");
//            ((CorrelationSkipGram) word2vec).addTrainedCorrelation(simFile,"simlex");
            ((CorrelationSkipGram) word2vec).addTrainedCorrelatedSpace(imageFile, true, "visual");
        }

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
