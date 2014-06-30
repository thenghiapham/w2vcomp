package demo;

import io.MaltInputStream;
import io.sentence.MaltSentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.MaltPhraseInputStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import vocab.Vocab;
import vocab.VocabEntry;
import vocab.VocabEntryFilter;
import vocab.filter.BooleanVocabFilter;
import vocab.filter.MinFrequencyVocabFilter;
import vocab.filter.SingleWordVocabFilter;
import word2vec.CBowWord2Vec;
import demo.TestConstants;
import dependency.DependencyNode;

public class PhraseVectorLearning {
    public static void main(String[] args) {
        CBowWord2Vec word2vec = new CBowWord2Vec(200, 5, true, 0, (float) 0);
        // CBowWord2Vec word2vec = new SimpleWord2Vec(200, 5, false, 10, (float)
        // 0);
        String trainFiles = TestConstants.GZIP_TRAIN_FILES;
        String outputFile = TestConstants.GZIP_VECTOR_FILE;
        String vocabFile = TestConstants.GZIP_VOCABULARY_FILE;
        String initFile = TestConstants.GZIP_INITIALIZATION_FILE;
        System.out.println("Starting training using file " + trainFiles);
        String[] files = trainFiles.split(";");

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(0);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            try {

                LinkedList<WordInputStream> inputStreams = new LinkedList<WordInputStream>();
                for (int i = 0; i < files.length; i++) {
                    MaltPhraseInputStream inputStream = new MaltPhraseInputStream(
                            new MaltInputStream(files[i],
                                    DependencyNode.LEMMA_POS, true));
                    inputStreams.add(inputStream);
                }
                CombinedWordInputStream wordInputStream = new CombinedWordInputStream(
                        inputStreams);
                vocab.learnVocabFromTrainStream(wordInputStream);

                // filter based on phrase type and min frequency
                SingleWordVocabFilter wordFilter = new SingleWordVocabFilter(
                        DependencyNode.LEMMA_POS);
                MinFrequencyVocabFilter min50Filter = new MinFrequencyVocabFilter(
                        50);
                MinFrequencyVocabFilter min20Filter = new MinFrequencyVocabFilter(
                        20);
                MinFrequencyVocabFilter min3Filter = new MinFrequencyVocabFilter(
                        3);
                SVFilter svFilter = new SVFilter();
                ANFilter anFilter = new ANFilter();
                VocabEntryFilter sv3Filter = new BooleanVocabFilter(
                        new VocabEntryFilter[] { svFilter, min3Filter },
                        BooleanVocabFilter.AND_FILTER);
                VocabEntryFilter an20Filter = new BooleanVocabFilter(
                        new VocabEntryFilter[] { anFilter, min20Filter },
                        BooleanVocabFilter.AND_FILTER);
                VocabEntryFilter word50Filter = new BooleanVocabFilter(
                        new VocabEntryFilter[] { wordFilter, min50Filter },
                        BooleanVocabFilter.AND_FILTER);
                VocabEntryFilter finalFilter = new BooleanVocabFilter(
                        new VocabEntryFilter[] { word50Filter, an20Filter,
                                sv3Filter }, BooleanVocabFilter.OR_FILTER);

                vocab.applyFilter(finalFilter);

                // save vocabulary
                vocab.saveVocab(vocabFile);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        word2vec.setVocab(vocab);

        word2vec.initNetwork(initFile);

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            for (int i = 0; i < files.length; i++) {
                SentenceInputStream inputStream = new MaltSentenceInputStream(
                        new MaltInputStream(files[i], DependencyNode.LEMMA_POS,
                                true));
                inputStreams.add(inputStream);
            }

            // ArrayList<SentenceInputStream> inputStreams = new
            // ArrayList<SentenceInputStream>();
            // inputStreams.add(sentenceInputStream);
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}

class SVFilter implements VocabEntryFilter {
    @Override
    public boolean isFiltered(VocabEntry entry) {
        // TODO Auto-generated method stub
        String[] elements = entry.word.split("-n_");
        return (elements.length != 2 || !elements[1].endsWith("-v")
                || elements[1].equals("be-v") || elements[1].equals("have-v") || elements[1]
                    .equals("do-v"));
    }
}

class ANFilter implements VocabEntryFilter {
    @Override
    public boolean isFiltered(VocabEntry entry) {
        // TODO Auto-generated method stub
        String[] elements = entry.word.split("-j_");
        return (elements.length != 2 || !elements[1].endsWith("-n"));
    }
}