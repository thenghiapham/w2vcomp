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

import common.DataStructureUtils;

public class SemanticSpace {
    String[]                 words;
    HashMap<String, Integer> word2Index;
    double[][]                vectors;
    int                      vectorSize;

    public SemanticSpace(int wordNumber, int vectorSize) {
        vectors = new double[wordNumber][vectorSize];
        words = new String[wordNumber];
        word2Index = new HashMap<String, Integer>();
        this.vectorSize = vectorSize;
    }

    public SemanticSpace(List<String> wordList, List<double[]> vectorList) {
        words = DataStructureUtils.stringListToArray(wordList);
        word2Index = DataStructureUtils.arrayToMap(words);
        vectors = DataStructureUtils.arrayListTo2dArray(vectorList);
        vectorSize = vectors[0].length;
    }

    public static SemanticSpace readSpace(String vectorFile) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(
                    new BufferedInputStream(new FileInputStream(vectorFile)));
            String firstWord = readWord(inputStream);
            String secondWord = readWord(inputStream);
            int wordNumber = Integer.parseInt(firstWord);
            int vectorSize = Integer.parseInt(secondWord);
            SemanticSpace result = new SemanticSpace(wordNumber, vectorSize);
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
                double[] vector = vectors[i];
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

    public static SemanticSpace importSpace(String textFile) {
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
            SemanticSpace result = new SemanticSpace(words, vectors);
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
                vectors[i][j] = buffer.getFloat(j * 4);
            }
            word2Index.put(word, i);
            inputStream.read();
        }
    }

    public double[] getVector(String word) {
        Integer index = word2Index.get(word);
        if (word == null) {
            return null;
        } else {
            return vectors[index];
        }
    }

    public Neighbor[] getNeighbors(double[] vector, int noNeighbor) {
        Neighbor[] neighbors = new Neighbor[words.length];
        for (int i = 0; i < words.length; i++) {
            neighbors[i] = new Neighbor(words[i], Similarity.cosine(vector,
                    vectors[i]));
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        if (noNeighbor < words.length) {
            return Arrays.copyOfRange(neighbors, 0, noNeighbor);
        } else {
            return neighbors;
        }
    }

    public Neighbor[] getNeighbors(double[] vector, int noNeighbor,
            String[] excludedWords) {
        Neighbor[] neighbors = new Neighbor[words.length - excludedWords.length];
        HashSet<String> excludedDict = DataStructureUtils
                .arrayToSet(excludedWords);
        int neighborIndex = 0;
        for (int i = 0; i < words.length; i++) {
            if (!excludedDict.contains(words[i])) {
                neighbors[neighborIndex] = new Neighbor(words[i],
                        Similarity.cosine(vector, vectors[i]));
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
        double[] vector = getVector(word);
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

    public SemanticSpace getSubSpace(WordFilter filter) {
        ArrayList<String> newWordList = new ArrayList<String>();
        ArrayList<double[]> newVectors = new ArrayList<double[]>();
        for (int i = 0; i < words.length; i++) {
            if (!filter.isFiltered(words[i])) {
                newWordList.add(words[i]);
                newVectors.add(vectors[i]);
            }
        }
        return new SemanticSpace(newWordList, newVectors);
    }

    public SemanticSpace getSubSpace(Collection<String> wordList) {
        ArrayList<String> newWordList = new ArrayList<String>();
        ArrayList<double[]> newVectors = new ArrayList<double[]>();
        for (String word : wordList) {
            if (this.contains(word)) {
                newWordList.add(word);
                newVectors.add(this.getVector(word));
            }
        }
        return new SemanticSpace(newWordList, newVectors);
    }

}
