package common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a set of utility IO method 
 * @author pham
 */
public class IOUtils {

    /**
     * Print an integer array to standard out
     */
    public static void printInts(int[] array) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]);
            buffer.append(' ');
        }
        System.out.println(buffer.toString());
    }

    /**
     * Print a long array to standard out
     */
    public static void printLongs(long[] array) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]);
            buffer.append(' ');
        }
        System.out.println(buffer.toString());
    }
    
    public static ArrayList<String> readFile(String inputString) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputString));
            ArrayList<String> slResult = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null && !line.equals("")) {
                slResult.add(line);
                line = reader.readLine();
            }
            reader.close();
            return slResult;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
    }
    
    public static void printToFile(String outputFile, List<String> strings) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (String line: strings) {
                writer.write(line);
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void saveMatrix(String matrixFile, double[][] matrix, boolean binary) {
        // Save the word vectors
        // save number of words, length of each vector
        int numRow = matrix.length;
        int numColumn = matrix[0].length;
        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(matrixFile));
            String firstLine = "" + numRow + " " + numColumn
                    + "\n";
            os.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < matrix.length; i++) {
                if (binary) {
                    ByteBuffer buffer = ByteBuffer
                            .allocate(4 * numColumn);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    for (int j = 0; j < numColumn; j++) {
                        buffer.putFloat((float) matrix[i][j]);
                    }
                    os.write(buffer.array());
                } else {
                    StringBuffer sBuffer = new StringBuffer();
                    for (int j = 0; j < numColumn; j++) {
                        sBuffer.append("" + matrix[i][j] + " ");
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
}
