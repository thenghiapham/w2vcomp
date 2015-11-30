package word2vec;

import io.sentence.SentenceInputStream;
import io.sentence.SubSamplingSentenceInputStream;
import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;


import space.SemanticSpace;
import vocab.Vocab;

import common.HeatMapPanel;
import common.IOUtils;
import common.MenCorrelation;
import common.exception.ValueException;

/**
 * Still abstract class for learning words' vectors
 * Implement some common methods
 * @author thenghiapham
 *
 */
public abstract class SingleThreadWord2Vec extends AbstractWord2Vec {

    protected long oldWordCount;
    protected MenCorrelation men;
    protected SemanticSpace outputSpace;
    HashMap<String, String> goldStandard;
    HashSet<String> words;
    HashSet<String> objects;
    
    int WORD ;
    
    static final Logger LOGGER = Logger.getLogger(SingleThreadWord2Vec.class.getName());
    

    public SingleThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int negativeSamplesImages, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImages, subSample);
    }
    
    public SingleThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples,int negativeSamplesImage, double subSample, String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImage, subSample);
        men = new MenCorrelation(menFile);
    }
    
   
    public void readGoldStandard(String gFile){
        this.goldStandard = new HashMap<String, String>();
        this.words = new HashSet<String>();
        this.objects = new HashSet<String>();
        
        ArrayList<String> lines = IOUtils.readFile(gFile);
        for (String line:lines){
            String[] els = line.split("[\t| ]+");
            System.out.println("Reading "+els[0]+" translated to "+els[1]);
            
            this.goldStandard.put(els[0], els[1]);
            
            this.words.add(els[0]);
            this.objects.add(els[1]);
        }
        WORD = 0;
       
    }
 
    

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreamsSource, ArrayList<SentenceInputStream> inputStreamsTarget) {
        // single-threaded instead of multi-threaded
        oldWordCount = 0;
        wordCount = 0;
        trainWords = vocab_lang1.getTrainWords();
        System.err.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab_lang1.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab_lang1.getEntry(0).word);
        System.out.println("last word:"
                + vocab_lang1.getEntry(vocab_lang1.getVocabSize() - 1).word);
        
        
        if (men != null) {
            try {
                outputSpace = new SemanticSpace(vocab_lang1, weights0, false);
            } catch (ValueException e) {
                e.printStackTrace();
            }
        }
        
        int i = 0;
        for (SentenceInputStream inputStreamSource : inputStreamsSource) {
            //subsample only source side
            if (subSample > 0) {
                inputStreamSource = new SubSamplingSentenceInputStream(inputStreamSource, subSample);
            }
            
            SentenceInputStream inputStreamTarget = inputStreamsTarget.get(i);
            System.out.println(inputStreamTarget.getWordCount()+" "+inputStreamSource.getWordCount());
            i++;
            trainModelThread(inputStreamSource, inputStreamTarget);
        }
        System.out.println("total word count: " + wordCount);
    }
    
    
    public void trainModel(ArrayList<SentenceInputStream> inputStreamsSource, ArrayList<SentenceInputStream> inputStreamsTarget, 
            ArrayList<SentenceInputStream> inputStreamsSocial) {
        // single-threaded instead of multi-threaded
        oldWordCount = 0;
        wordCount = 0;
        trainWords = vocab_lang1.getTrainWords();
        System.err.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab_lang1.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab_lang1.getEntry(0).word);
        System.out.println("last word:"
                + vocab_lang1.getEntry(vocab_lang1.getVocabSize() - 1).word);
        
        
        if (men != null) {
            try {
                outputSpace = new SemanticSpace(vocab_lang1, weights0, false);
            } catch (ValueException e) {
                e.printStackTrace();
            }
        }
        
        int i = 0;
        for (SentenceInputStream inputStreamSource : inputStreamsSource) {
            //subsample only source side
            if (subSample > 0) {
                inputStreamSource = new SubSamplingSentenceInputStream(inputStreamSource, subSample);
            }
            
            SentenceInputStream inputStreamTarget = inputStreamsTarget.get(i);
            SentenceInputStream inputStreamSocial = inputStreamsSocial.get(i);
            System.out.println(inputStreamTarget.getWordCount()+" "+inputStreamSource.getWordCount());
            i++;
            trainModelThread(inputStreamSource, inputStreamTarget, inputStreamSocial);
        }
        System.out.println("total word count: " + wordCount);
    }
    
    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreamsSource, ArrayList<SentenceInputStream> inputStreamsTarget, Vocab vocab) {
        // single-threaded instead of multi-threaded
        oldWordCount = 0;
        wordCount = 0;
        trainWords = vocab.getTrainWords();
        System.out.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        
        
        if (men != null) {
            try {
                outputSpace = new SemanticSpace(vocab_lang1, weights0, false);
            } catch (ValueException e) {
                e.printStackTrace();
            }
        }
        
        int i = 0;
        for (SentenceInputStream inputStreamSource : inputStreamsSource) {
            //subsample only source side
            if (subSample > 0) {
                inputStreamSource = new SubSamplingSentenceInputStream(inputStreamSource, subSample);
            }
            
            SentenceInputStream inputStreamTarget = inputStreamsTarget.get(i);
            System.out.println(inputStreamTarget.getWordCount()+" "+inputStreamSource.getWordCount());
            i++;
            trainModelThread(inputStreamSource, inputStreamTarget);
        }
        System.out.println("total word count: " + wordCount);
    }

    void trainModelThread(SentenceInputStream inputStreamSource, SentenceInputStream inputStreamTarget) {
        oldWordCount = wordCount;
        long lastWordCount = wordCount;
        int updateEveryNWords = 100; //100 for initial implementation were results were reported
        int curSentence = 1;
        try {
            int iteration = 0;
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                boolean hasNextSentenceSource = inputStreamSource.readNextSentence(vocab_lang1);
                boolean hasNextSentenceTarget = inputStreamTarget.readNextSentence(vocab_lang2);
                
                //System.out.println("Current sentence is "+curSentence);
                curSentence++;
                if (!hasNextSentenceSource) break;

                int[] sentenceSource = inputStreamSource.getCurrentSentence();
                int[] sentenceTarget = inputStreamTarget.getCurrentSentence();
                
                // if end of file, finish
                if (sentenceSource.length == 0) {
                    continue;
                }

                // check word count
                // update alpha
                wordCount = oldWordCount + inputStreamSource.getWordCount();
              
                
                
                
                if (wordCount - lastWordCount > updateEveryNWords) {
                    iteration++;
                    
                    
                    // update alpha
                    alpha = starting_alpha
                            * (1 - (double) wordCount / (trainWords + 1));
                    if (alpha < starting_alpha * (1/updateEveryNWords)) {
                        alpha = starting_alpha * (1/updateEveryNWords);
                    }
                    if (men != null && outputSpace != null &&  iteration %1000 == 0) {
                        //System.out.println("correlation: " + men.evaluateSpaceSpearman2(outputSpace, images.getVisionSpace(),1)+" "+men.evaluateSpaceSpearman2(outputSpace, images.getVisionSpace(),2));
                        System.out.println("correlation: " + men.evaluateSpaceSpearman(outputSpace));
                        mmWordsPerRun = 0;
                        printStatistics();
                    }
                    if (iteration % 1000 == 0) {
                        System.out.println("Trained: " + wordCount + " words");
                        System.out.println("Training rate: " + alpha);
                        //System.out.println("Visual stuff "+imageProjectionLayer.normF());
                        
                    }
                    lastWordCount = wordCount;
                }
                
                        
                
                trainSentence(sentenceSource, sentenceTarget);
                
            }
        } catch (IOException | ValueException  e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void trainModelThread(SentenceInputStream inputStreamSource, SentenceInputStream inputStreamTarget, 
            SentenceInputStream inputStreamSocial) {
        oldWordCount = wordCount;
        long lastWordCount = wordCount;
        int updateEveryNWords = 100; //100 for initial implementation were results were reported
        int curSentence = 1;
        try {
            int iteration = 0;
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                boolean hasNextSentenceSource = inputStreamSource.readNextSentence(vocab_lang1);
                boolean hasNextSentenceTarget = inputStreamTarget.readNextSentence(vocab_lang2);
                boolean hasNextSentenceSocial = inputStreamSocial.readNextSentence(vocab_lang3);
                
                //System.out.println("Current sentence is "+curSentence);
                curSentence++;
                if (!hasNextSentenceSource) break;

                int[] sentenceSource = inputStreamSource.getCurrentSentence();
                int[] sentenceTarget = inputStreamTarget.getCurrentSentence();
                int[] sentenceSocial = inputStreamSocial.getCurrentSentence();
                
                // if end of file, finish
                if (sentenceSource.length == 0) {
                    continue;
                }

                // check word count
                // update alpha
                wordCount = oldWordCount + inputStreamSource.getWordCount();
              
                
                
                
                if (wordCount - lastWordCount > updateEveryNWords) {
                    iteration++;
                    
                    
                    // update alpha
                    alpha = starting_alpha
                            * (1 - (double) wordCount / (trainWords + 1));
                    if (alpha < starting_alpha * (1/updateEveryNWords)) {
                        alpha = starting_alpha * (1/updateEveryNWords);
                    }
                    if (men != null && outputSpace != null &&  iteration %1000 == 0) {
                        //System.out.println("correlation: " + men.evaluateSpaceSpearman2(outputSpace, images.getVisionSpace(),1)+" "+men.evaluateSpaceSpearman2(outputSpace, images.getVisionSpace(),2));
                        System.out.println("correlation: " + men.evaluateSpaceSpearman(outputSpace));
                        mmWordsPerRun = 0;
                        printStatistics();
                    }
                    if (iteration % 1000 == 0) {
                        System.out.println("Trained: " + wordCount + " words");
                        System.out.println("Training rate: " + alpha);
                        //System.out.println("Visual stuff "+imageProjectionLayer.normF());
                        
                    }
                    lastWordCount = wordCount;
                }
                
                        
                
                trainSentence(sentenceSource, sentenceTarget, sentenceSocial);
                
            }
        } catch (IOException | ValueException  e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void trainPhrases(Phrase[] phrases, int[] sentence) {
        /*for (Phrase phrase : phrases) {
            trainSinglePhrase(phrase, sentence);
        }*/
    }
    
    public void printStatistics() {
        LOGGER.log(Level.INFO, "correlation: " + men.evaluateSpacePearson(outputSpace));
    }
    
    public SemanticSpace getSpace(){
        return this.outputSpace;
    }

    public abstract void trainSinglePhrase(Phrase phrase,
            int[] sentence);

    public abstract void trainSentence(int[] sentenceSource, int[] sentenceTarget);
    
    public abstract void trainSentence(int[] sentenceSource, int[] sentenceTarget, int[] sentenceSocial);
    
}
