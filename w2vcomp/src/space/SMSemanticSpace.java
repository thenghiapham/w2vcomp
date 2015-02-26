package space;

import io.word.WordFilter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;

import common.DataStructureUtils;
import common.SimpleMatrixUtils;
import common.exception.ValueException;

/**
 * short for SimpleMatrix Semantic Space
 * @author thenghiapham
 *
 */
public class SMSemanticSpace implements SemanticSpace {
    String[]                 words;
    HashMap<String, Integer> word2Index;
    SimpleMatrix               vectors;
    int                      vectorSize;

    protected SMSemanticSpace(int wordNumber, int vectorSize) {
        vectors = new SimpleMatrix(wordNumber,vectorSize);
        words = new String[wordNumber];
        word2Index = new HashMap<String, Integer>();
        this.vectorSize = vectorSize;
    }

    public SMSemanticSpace(List<String> wordList, List<double[]> vectorList) {
        words = DataStructureUtils.stringListToArray(wordList);
        word2Index = DataStructureUtils.arrayToMap(words);
        vectors = new SimpleMatrix(DataStructureUtils.arrayListTo2dArray(vectorList));
        vectorSize = vectors.numCols();
    }
    
    public SMSemanticSpace(List<String> wordList, SimpleMatrix vectors) {
        words = DataStructureUtils.stringListToArray(wordList);
        word2Index = DataStructureUtils.arrayToMap(words);
        this.vectors = vectors;
        vectorSize = vectors.numCols();
    }
    
    public SMSemanticSpace(String[] words, double[][] vectors) {
        this.words = words;
        this.vectors = new SimpleMatrix(vectors);
        vectorSize = vectors[0].length;
        word2Index = DataStructureUtils.arrayToMap(words);
    }
    
    public SMSemanticSpace(String[] words, SimpleMatrix vectors) {
        this.words = words;
        this.vectors = vectors;
        vectorSize = vectors.numCols();
        word2Index = DataStructureUtils.arrayToMap(words);
    }
    
    public SMSemanticSpace(Vocab vocab, SimpleMatrix vectors, boolean copy){
        if (vocab.getVocabSize() != vectors.numRows() && vocab.getVocabSize() != vectors.numRows() - 1) {
            throw new ValueException("vocab and vectors must have the same size");
        } else {
            
            vectorSize = vectors.numCols();
            
            if (!copy) {
                this.vectors = vectors;
            } else {
                this.vectors = new SimpleMatrix(vectors);
            }
            
            int vocabSize = vocab.getVocabSize();
            words = new String[vocabSize];
            for (int i = 0; i < vocabSize; i++) {
                words[i] = vocab.getEntry(i).word;
            }
            
            word2Index = DataStructureUtils.arrayToMap(words);
        }
    }

    public static SMSemanticSpace readSpace(String vectorFile) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(
                    new BufferedInputStream(new FileInputStream(vectorFile)));
            String firstWord = readWord(inputStream);
            String secondWord = readWord(inputStream);
            int wordNumber = Integer.parseInt(firstWord);
            int vectorSize = Integer.parseInt(secondWord);
            SMSemanticSpace result = new SMSemanticSpace(wordNumber, vectorSize);
            result.readSpace(inputStream);
            inputStream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void exportSpace(String textFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
            for (int i = 0; i < words.length; i++) {
                writer.write(words[i] + "\t");
                double[] vector = vectors.extractVector(true, i).getMatrix().getData();
                for (int j = 0; j < vectorSize; j++) {
                    writer.write("" + vector[j]);
                    if (j < vectorSize - 1) {
                        writer.write("\t");
                    } else {
                        writer.write("\n");
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[] parseVector(String[] elements, int startIndex) {
        double[] result = new double[elements.length - startIndex];
        for (int i = startIndex; i < elements.length; i++) {
            result[i - startIndex] = Float.parseFloat(elements[i]);
        }
        return result;
    }

    public static SMSemanticSpace importSpace(String textFile) {
        ArrayList<String> words = new ArrayList<String>();
        ArrayList<double[]> vectors = new ArrayList<double[]>();
        // int vectorSize = 0;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(textFile));
            String line = reader.readLine();
            if (line != null && !line.equals("")) {
                String[] elements = line.split("( |\\t)");
                // vectorSize = elements.length - 1;
                double[] vector = parseVector(elements, 1);
                words.add(elements[0]);
                vectors.add(vector);

                line = reader.readLine();
                while (line != null && !line.equals("")) {
                    elements = line.split("( |\\t)");
                    vector = parseVector(elements, 1);
                    words.add(elements[0]);
                    vectors.add(vector);
                    line = reader.readLine();
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (words.size() == 0) {
            return null;
        } else {
            SMSemanticSpace result = new SMSemanticSpace(words, vectors);
            return result;
        }
    }

    public static String readWord(InputStream inputStream) throws IOException {
        StringBuffer buffer = new StringBuffer();
        while (true) {
            int nextByte = inputStream.read();
            if (nextByte == -1 || nextByte == ' ' || nextByte == '\n') {
                if (nextByte == -1 && buffer.length() == 0) {
                    return null;
                } else {
                    break;
                }
            } else {
                buffer.append((char) nextByte);
            }
        }
        return buffer.toString();
    }

    public void readSpace(InputStream inputStream) throws IOException {
        byte[] rowData = new byte[vectorSize * 4];
        ByteBuffer buffer = ByteBuffer.wrap(rowData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < words.length; i++) {
            String word = readWord(inputStream);
            words[i] = word;
            inputStream.read(rowData);
            for (int j = 0; j < vectorSize; j++) {
                vectors.set(i, j, buffer.getFloat(j * 4));
            }
            word2Index.put(word, i);
            inputStream.read();
        }
    }

    // row vector
    public SimpleMatrix getVector(String word) {
        Integer index = word2Index.get(word);
        if (index == null) {
            return null;
        } else {
            return vectors.extractVector(true, index);
        }
    }
    
    public double getSim(String word1, String word2) {
        if (word2Index.containsKey(word1) && word2Index.containsKey(word2)) {
            int index1 = word2Index.get(word1);
            int index2 = word2Index.get(word2);
            return Similarity.cosine(vectors.extractVector(true, index1),vectors.extractVector(true, index2));
        } else {
            return 0;
        }
    }

    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor) {
        Neighbor[] neighbors = new Neighbor[words.length];
        double[] sims = Similarity.massCosine(vectors, vector).getMatrix().getData();
        
        for (int i = 0; i < words.length; i++) {
            neighbors[i] = new Neighbor(words[i], sims[i]);
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        if (noNeighbor < words.length) {
            return Arrays.copyOfRange(neighbors, 0, noNeighbor);
        } else {
            return neighbors;
        }
    }

    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor,
            String[] excludedWords) {
        Neighbor[] neighbors = new Neighbor[words.length - excludedWords.length];
        HashSet<String> excludedDict = DataStructureUtils
                .arrayToSet(excludedWords);
        int neighborIndex = 0;
        double[] sims = Similarity.massCosine(vectors, vector).getMatrix().getData();
        for (int i = 0; i < words.length; i++) {
            if (!excludedDict.contains(words[i])) {
                neighbors[neighborIndex] = new Neighbor(words[i],sims[i]);
                neighborIndex++;
            }
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        if (noNeighbor < words.length) {
            return Arrays.copyOfRange(neighbors, 0, noNeighbor);
        } else {
            return neighbors;
        }
    }

    public Neighbor[] getNeighbors(String word, int noNeighbor) {
        SimpleMatrix vector = getVector(word);
        if (vector == null) {
            return null;
        } else {
            return getNeighbors(vector, noNeighbor, new String[] { word });
        }
    }

    public static void printVector(double[] vector) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < vector.length; i++) {
            buffer.append(vector[i]);
            buffer.append(' ');
        }
        System.out.println(buffer.toString());
    }

    public boolean contains(String word) {
        return word2Index.containsKey(word);
    }

    public SMSemanticSpace getSubSpace(WordFilter filter) {
        ArrayList<String> newWordList = new ArrayList<String>();
        ArrayList<Integer> newRows = new ArrayList<Integer>();
        
        for (int i = 0; i < words.length; i++) {
            if (!filter.isFiltered(words[i])) {
                newWordList.add(words[i]);
                newRows.add(i);
            }
        }
        return new SMSemanticSpace(newWordList, SimpleMatrixUtils.getRows(vectors, DataStructureUtils.intListToArray(newRows)));
    }

    public SMSemanticSpace getSubSpace(Collection<String> wordList) {
        ArrayList<String> newWordList = new ArrayList<String>();
        ArrayList<Integer> newRows = new ArrayList<Integer>();
        for (String word : wordList) {
            if (this.contains(word)) {
                newWordList.add(word);
                newRows.add(word2Index.get(word));
            }
        }
        return new SMSemanticSpace(newWordList, SimpleMatrixUtils.getRows(vectors, DataStructureUtils.intListToArray(newRows)));
    
    }
    
    public int getVectorSize() {
        return vectorSize;
    }
}
