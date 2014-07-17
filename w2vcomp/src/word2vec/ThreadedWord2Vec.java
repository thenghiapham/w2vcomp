package word2vec;

import io.word.RandomAccessWordStream;
import io.word.WordInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import vocab.Vocab;
import vocab.VocabEntry;

import common.MathUtils;
import demo.TestConstants;

public class ThreadedWord2Vec {
    public static final double STARTING_ALPHA      = 0.025;
    public static final int    MAX_SENTENCE_LENGTH = 1000;
    public static final int    MAX_EXP             = 6;
    public static final int    EXP_TABLE_SIZE      = 1000;
    public static int          tableSize           = 100000000;

    double                     alpha;
    protected Vocab            vocab;
    boolean                    hierarchicalSoftmax;
    int                        negativeSamples;
    double                      subSample;
    int                        layer1Size;
    boolean                    cBow                = true;
    int                        threadNum           = 4;

    int[]                      table;
    int                        wordCount;
    double[][]                  syn0, syn1, syn1neg;
    double[]                    expTable;
    int                        windowSize;
    long                       trainWords;
    Random                     rand;
    int                        minFrequency;

    Integer                    wordCountLock       = new Integer(0);

    public ThreadedWord2Vec(int layer1Size, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample,
            int minFrequency) {
        this.layer1Size = layer1Size;
        this.hierarchicalSoftmax = hierarchicalSoftmax;
        this.windowSize = windowSize;
        this.subSample = subSample;
        this.negativeSamples = negativeSamples;
        this.minFrequency = minFrequency;

        this.rand = new Random();
        this.vocab = new Vocab(minFrequency);
        initExpTable();
    }

    public void saveInitialization(String weightFile) {
        try {
            BufferedOutputStream outStream = new BufferedOutputStream(
                    new FileOutputStream(weightFile));
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(vocab.getVocabSize());
            buffer.putInt(layer1Size);
            outStream.write(buffer.array());
            for (int i = 0; i < vocab.getVocabSize(); i++) {
                buffer = ByteBuffer.allocate(4 * layer1Size);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int j = 0; j < layer1Size; j++) {
                    buffer.putFloat((float) syn0[i][j]);
                }
                outStream.write(buffer.array());
            }
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadInitialization(String weightFile) {
        try {
            BufferedInputStream inStream = new BufferedInputStream(
                    new FileInputStream(weightFile));
            byte[] array = new byte[4];
            inStream.read(array);
            ByteBuffer buffer = ByteBuffer.wrap(array);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int newVocabSize = buffer.getInt(0);
            inStream.read(array);
            int newLayer1Size = buffer.getInt(0);
            System.out.println("new vocab size:" + newVocabSize);
            System.out.println("new layer1 size:" + newLayer1Size);
            for (int i = 0; i < vocab.getVocabSize(); i++) {
                for (int j = 0; j < layer1Size; j++) {
                    inStream.read(array);
                    syn0[i][j] = buffer.getFloat(0);
                }
            }
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initNet(String initFile) {
        int vocabSize = vocab.getVocabSize();
        syn0 = new double[vocabSize][layer1Size];
        if (hierarchicalSoftmax) {
            syn1 = new double[vocabSize][layer1Size];
        }
        if (negativeSamples > 0) {
            syn1neg = new double[vocabSize][layer1Size];
        }
        boolean readInit = (new File(initFile)).exists();
        if (!readInit) {
            randomInit();
            saveInitialization(initFile);
        } else {
            loadInitialization(initFile);
        }

        vocab.assignCode();
    }

    void randomInit() {
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            for (int j = 0; j < layer1Size; j++) {
                syn0[i][j] = (double) (rand.nextFloat() - 0.5) / layer1Size;
            }
        }
    }

    public void initExpTable() {
        expTable = new double[EXP_TABLE_SIZE];
        for (int i = 0; i < EXP_TABLE_SIZE; i++) {
            expTable[i] = (double) Math.exp((i / (double) EXP_TABLE_SIZE * 2 - 1)
                    * MAX_EXP); // Precompute the exp() table
            expTable[i] = (double) expTable[i] / (expTable[i] + 1); // Precompute
                                                                   // f(x) = x /
                                                                   // (x + 1)
        }
    }

    /**
     * Create an unigram table to randomly generate a word based. The
     * probability of generating a word corresponds to its frequency^3/4
     */
    void initUnigramTable() {
        long trainWordsPow = 0;
        double sumPow;
        double power = (double) 0.75;
        int vocabSize = vocab.getVocabSize();
        table = new int[tableSize];
        for (int i = 0; i < vocabSize; i++) {
            trainWordsPow += Math.pow(vocab.getEntry(i).frequency, power);
        }
        int index = 0;
        sumPow = (double) Math.pow(vocab.getEntry(index).frequency, power)
                / trainWordsPow;
        for (int i = 0; i < tableSize; i++) {
            table[i] = index;
            if (i / (double) tableSize > sumPow) {
                index++;
                if (index < vocabSize) {
                    sumPow += Math.pow(vocab.getEntry(index).frequency, power)
                            / trainWordsPow;
                } else {
                    System.out.println("what does it mean here");
                }
            }
            if (index >= vocabSize)
                index = vocabSize - 1;
        }
    }

    void trainModel(String trainFile, String outputFile, String vocabFile,
            String initFile) {
        System.out.println("Starting training using file " + trainFile);
        alpha = STARTING_ALPHA;

        boolean learnVocab = !(new File(vocabFile)).exists();
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            vocab.learnVocabFromTrainFile(trainFile);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }
        trainWords = vocab.getTrainWords();

        initNet(initFile);

        if (negativeSamples > 0) {
            initUnigramTable();
        }

        // create threads and join
        // parallel here
        // single threaded for now
        System.out.println("Start training");
        Thread[] threads = new Thread[threadNum];
        for (int index = 0; index < threadNum; index++) {
            TrainRunnable r = new TrainRunnable(trainFile, threadNum, index);
            Thread t = new Thread(r);
            t.start();
            threads[index] = t;
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // trainModelThread(trainFile, 4, 1);
        saveVector(outputFile, true);

    }

    public void saveVector(String outputFile, boolean binary) {
        // Save the word vectors
        // save number of words, length of each vector
        int vocabSize = vocab.getVocabSize();

        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(outputFile));
            String firstLine = "" + vocabSize + " " + layer1Size + "\n";
            os.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < vocabSize; i++) {
                VocabEntry word = vocab.getEntry(i);
                os.write((word.word + " ").getBytes("UTF-8"));
                if (binary) {
                    ByteBuffer buffer = ByteBuffer.allocate(4 * layer1Size);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    for (int j = 0; j < layer1Size; j++) {
                        buffer.putFloat((float) syn0[i][j]);
                    }
                    os.write(buffer.array());
                } else {
                    StringBuffer sBuffer = new StringBuffer();
                    for (int j = 0; j < layer1Size; j++) {
                        sBuffer.append("" + syn0[i][j] + " ");
                    }
                    os.write(sBuffer.toString().getBytes());
                }
                os.write("\n".getBytes());
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> readSentence(WordInputStream inputStream) {
        ArrayList<Integer> sentence = new ArrayList<Integer>();
        int readTrainedWords = 0;
        while (true) {
            // read the next word & the word index
            String word = "";
            try {
                word = inputStream.readWord();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            if ("".equals(word))
                break;
            int wordIndex = vocab.getWordIndex(word);

            // if the word is not in the vocabulary, continue
            if (wordIndex == -1)
                continue;

            readTrainedWords++;

            // end of sentence -> break;
            if (wordIndex == 0) {
                System.out.println("end of sentence: " + word);
                break;
            }

            // sub sampling
            // The subsampling randomly discards frequent words while keeping
            // the ranking same
            if (subSample > 0) {
                double threshold = (double) (Math
                        .sqrt(vocab.getEntry(wordIndex).frequency
                                / (subSample * trainWords)) + 1)
                        * (subSample * trainWords)
                        / vocab.getEntry(wordIndex).frequency;
                if (threshold < rand.nextFloat())
                    continue;
            }
            sentence.add(wordIndex);
            // break if sentence is too long
            if (sentence.size() >= MAX_SENTENCE_LENGTH)
                break;

        }
        synchronized (wordCountLock) {
            wordCount += readTrainedWords;
        }
        // System.out.println("sentence length: " + sentence.size());
        return sentence;
    }

    void trainModelThread(String trainFile, int threadNum, int index) {

        wordCount = 0;
        int lastWordCount = 0;

        try {
            // seek file
            WordInputStream inputStream = new RandomAccessWordStream(trainFile,
                    threadNum, index, Vocab.MAX_LENGTH);
            while (true) {
                // print information & output when reach some point

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary

                // check word count
                // update alpha
                ArrayList<Integer> sentence = readSentence(inputStream);
                synchronized (wordCountLock) {
                    if (wordCount - lastWordCount > 10000) {
                        System.out.println("Thread: " + index + " Trained: "
                                + wordCount + " words");
                        alpha = STARTING_ALPHA
                                * (1 - (double) wordCount / (trainWords + 1));
                        if (alpha < STARTING_ALPHA * 0.0001) {
                            alpha = STARTING_ALPHA * 0.0001;
                        }
                        System.out.println("Training rate: " + alpha);
                        lastWordCount = wordCount;
                    }
                    if (wordCount > trainWords) {
                        System.out.println("sentence size: " + sentence.size());
                    }
                }

                // if end of file, finish

                if (sentence.size() == 0)
                    break;
                if (cBow) {
                    trainSentenceCBow(toArray(sentence));
                } else {
                    trainSentenceSKipGram(toArray(sentence));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] toArray(ArrayList<Integer> sentence) {
        int[] result = new int[sentence.size()];
        for (int i = 0; i < sentence.size(); i++) {
            result[i] = sentence.get(i);
        }
        return result;

    }

    public void trainSentenceCBow(int[] sentence) {
        // train with the sentence
        double[] neurons1 = new double[layer1Size];
        double[] neuron1error = new double[layer1Size];
        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        for (int wordPosition = 0; wordPosition < sentence.length; wordPosition++) {

            int wordIndex = sentence[wordPosition];

            // no way it will go here
            if (wordIndex == -1)
                continue;

            for (int i = 0; i < layer1Size; i++) {
                neurons1[i] = 0;
                neuron1error[i] = 0;
            }

            // random window size?
            int start = rand.nextInt(windowSize);

            // CBOW
            // train the cbow architecture
            // in -> hidden
            synchronized (syn0) {
                for (int i = start; i < windowSize * 2 + 1 - start; i++) {

                    if (i != windowSize) {
                        int currentPos = wordPosition - windowSize + i;
                        if (currentPos < 0 || currentPos >= sentenceLength)
                            continue;
                        iWordIndex = sentence[currentPos];
                        if (iWordIndex == -1) {
                            // System.out.println("shouldn't be here");
                            continue;
                        }

                        for (int j = 0; j < layer1Size; j++)
                            neurons1[j] += syn0[iWordIndex][j];

                    }
                }
                if (hierarchicalSoftmax) {
                    VocabEntry word = vocab.getEntry(wordIndex);
                    for (int bit = 0; bit < word.code.length(); bit++) {

                        double f = 0;
                        int iParentIndex = word.ancestors[bit];

                        // Propagate hidden -> output
                        for (int i = 0; i < layer1Size; i++) {
                            f += neurons1[i] * syn1[iParentIndex][i];
                        }
                        if (f <= -MAX_EXP || f >= MAX_EXP) {
                            continue;
                        } else {
                            f = 1 - (double) (1.0 / (1.0 + Math.exp(f)));
                        }

                        // 'g' is the gradient multiplied by the learning rate
                        double gradient = (double) ((1 - (word.code.charAt(bit) - 48) - f) * alpha);

                        // Propagate errors output -> hidden
                        for (int i = 0; i < layer1Size; i++) {
                            neuron1error[i] += gradient * syn1[iParentIndex][i];
                        }
                        // Learn weights hidden -> output
                        for (int i = 0; i < layer1Size; i++) {
                            syn1[iParentIndex][i] += gradient * neurons1[i];
                        }

                    }
                }

                // NEGATIVE SAMPLING
                if (negativeSamples > 0) {
                    int target;
                    int label;
                    for (int j = 0; j < negativeSamples + 1; j++) {
                        if (j == 0) {
                            target = wordIndex;
                            label = 1;
                        } else {
                            int randInt = rand.nextInt(tableSize);
                            target = table[randInt];
                            if (target == 0)
                                target = rand.nextInt(vocab.getVocabSize() - 1) + 1;
                            if (target == wordIndex)
                                continue;
                            label = 0;
                        }

                        double f = 0;
                        for (int i = 0; i < layer1Size; i++) {
                            f += neurons1[i] * syn1neg[target][i];
                        }
                        double gradient;
                        if (f > MAX_EXP) {
                            gradient = (double) ((label - 1) * alpha);
                        } else if (f < -MAX_EXP) {
                            gradient = (double) ((label - 0) * alpha);
                        } else {
                            // gradient = (double) ((label - expTable[(int)((f +
                            // MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))]) *
                            // alpha);
                            gradient = (double) ((label - MathUtils
                                    .sigmoid(f)) * alpha);
                        }

                        for (int i = 0; i < layer1Size; i++) {
                            neuron1error[i] += gradient * syn1neg[target][i];
                        }
                        for (int i = 0; i < layer1Size; i++) {
                            syn1neg[target][i] += gradient * neurons1[i];
                        }
                    }
                }

                // hidden -> in
                for (int i = start; i < windowSize * 2 + 1 - start; i++) {
                    if (i != windowSize) {
                        int iPos = wordPosition - windowSize + i;
                        if (iPos < 0 || iPos >= sentenceLength)
                            continue;
                        iWordIndex = sentence[iPos];
                        if (iWordIndex == -1)
                            continue;
                        for (int j = 0; j < layer1Size; j++) {
                            syn0[iWordIndex][j] += neuron1error[j];
                        }

                    }
                }
            }

        }

    }

    public void trainSentenceSKipGram(int[] sentence) {
        // train with the sentence
        double[] neurons1 = new double[layer1Size];
        double[] neuron1error = new double[layer1Size];
        int sentenceLength = sentence.length;
        for (int wordPosition = 0; wordPosition < sentence.length; wordPosition++) {

            int wordIndex = sentence[wordPosition];

            // no way it will go here
            if (wordIndex == -1)
                continue;

            for (int i = 0; i < layer1Size; i++) {
                neurons1[i] = 0;
                neuron1error[i] = 0;
            }

            // random some window size word?
            int start = rand.nextInt(windowSize);

            VocabEntry word = vocab.getEntry(wordIndex);
            for (int i = start; i < windowSize * 2 + 1 - start; i++) {
                if (i != windowSize) {
                    int iPos = wordPosition - windowSize + i;
                    if (iPos < 0 || iPos >= sentenceLength)
                        continue;
                    int iWordIndex = sentence[iPos];
                    if (iWordIndex == -1)
                        continue;

                    for (int j = 0; j < layer1Size; j++)
                        neuron1error[j] = 0;

                    // HIERARCHICAL SOFTMAX
                    if (hierarchicalSoftmax) {
                        for (int bit = 0; bit < word.code.length(); bit++) {
                            double f = 0;
                            int iParentIndex = word.ancestors[bit];
                            // Propagate hidden -> output
                            for (int j = 0; j < layer1Size; j++) {
                                f += syn0[iWordIndex][j]
                                        * syn1[iParentIndex][j];
                            }

                            if (f <= -MAX_EXP || f >= MAX_EXP) {
                                continue;
                            } else
                                f = 1 - (double) (1.0 / (1.0 + Math.exp(f)));
                            // f = expTable[(int)((f + MAX_EXP) *
                            // (EXP_TABLE_SIZE / MAX_EXP / 2))];
                            System.out.println("exp f: " + f);

                            // 'g' is the gradient multiplied by the learning
                            // rate
                            double gradient = (double) ((1 - (word.code
                                    .charAt(bit) - 48) - f) * alpha);
                            // Propagate errors output -> hidden
                            for (int j = 0; j < layer1Size; j++) {
                                neuron1error[j] += gradient
                                        * syn1[iParentIndex][j];
                            }
                            // Learn weights hidden -> output
                            for (int j = 0; j < layer1Size; j++) {
                                syn1[iParentIndex][j] += gradient
                                        * syn0[iWordIndex][j];
                            }
                        }
                    }

                    // NEGATIVE SAMPLING
                    if (negativeSamples > 0) {
                        for (int l = 0; l < negativeSamples + 1; l++) {
                            int target;
                            int label;

                            if (l == 0) {
                                target = wordIndex;
                                label = 1;
                            } else {
                                target = rand.nextInt(tableSize);
                                if (target == 0) {
                                    target = rand
                                            .nextInt(vocab.getVocabSize() - 1) + 1;
                                }
                                if (target == wordIndex)
                                    continue;
                                label = 0;
                            }
                            double f = 0;
                            double gradient;
                            for (int j = 0; j < layer1Size; j++) {
                                f += syn0[iWordIndex][j] * syn1neg[target][j];
                            }
                            if (f > MAX_EXP)
                                gradient = (double) ((label - 1) * alpha);
                            else if (f < -MAX_EXP)
                                gradient = (double) ((label - 0) * alpha);
                            else
                                gradient = (double) ((label - expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE
                                        / MAX_EXP / 2))]) * alpha);
                            for (int j = 0; j < layer1Size; j++) {
                                neuron1error[j] += gradient
                                        * syn1neg[target][j];
                            }
                            for (int j = 0; j < layer1Size; j++) {
                                syn1neg[target][j] += gradient
                                        * syn0[iWordIndex][j];
                            }
                        }
                    }

                    // Learn weights input -> hidden
                    for (int j = 0; j < layer1Size; j++) {
                        syn0[iWordIndex][j] += neuron1error[j];
                    }
                }

            }

        }

    }

    public void printInformation() {
        System.out.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("layer1 size: " + layer1Size);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
    }

    public static void main(String[] args) {
        // ThreadedWord2Vec word2vec = new ThreadedWord2Vec(200, 5, true, 0,
        // (double) 0, 50);
        ThreadedWord2Vec word2vec = new ThreadedWord2Vec(200, 5, false, 10,
                (double) 0, 50);
        String trainFile = TestConstants.TRAIN_FILE;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        String initFile = TestConstants.INITIALIZATION_FILE;
        word2vec.trainModel(trainFile, outputFile, vocabFile, initFile);

    }

    class TrainRunnable implements Runnable {
        private int    threadNum;
        private int    index;
        private String trainFile;

        public TrainRunnable(String trainFile, int threadNum, int index) {
            this.trainFile = trainFile;
            this.threadNum = threadNum;
            this.index = index;
        }

        @Override
        public void run() {
            trainModelThread(trainFile, threadNum, index);
        }

    }
}
