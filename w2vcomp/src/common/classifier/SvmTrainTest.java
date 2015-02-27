package common.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import common.exception.UnimplementedException;

public class SvmTrainTest {
    String svmDir;
    public SvmTrainTest(String svmDir) {
        this.svmDir = svmDir; 
    }
    
    public static int[] getIndices(int[] div, int divIndex) {
        ArrayList<Integer> indexList = new ArrayList<Integer>();
        for (int i = 0; i < div.length; i++) {
            if (div[i] == divIndex) {
                indexList.add(i);
            }
        }
        int[] indices = new int[indexList.size()];
        for (int i = 0; i < 0; i++) {
            indices[i] = indexList.get(i);
        }
        return indices;
    }
    
    public static double[][] extractData(double[][] features, int[] indices) {
        double[][] result = new double[indices.length][];
        for (int i = 0; i < indices.length; i++) {
            result[i] = features[indices[i]];
        }
        return result;
    }
    
    public static String[] extractLabels(String[] labels, int[] indices) {
        String[] result = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = labels[indices[i]];
        }
        return result;
    }
    
    public double trainTest(String[] trainLabels, double[][] trainData, String[] testLabels, double[][] testData, String optionString) {
        Runtime rt = Runtime.getRuntime();
        ArrayList<String> trainCommandList = new ArrayList<String>();
        ArrayList<String> testCommandList = new ArrayList<String>();
        
        trainCommandList.add(svmDir + "/svm-train");
        testCommandList.add(svmDir + "/svm-predict");
        if (optionString.length() > 0)
        {
            String[] options = optionString.split(" ");
            for (String option: options) {
                trainCommandList.add(option);
            }
        }
        File trainFile = null;
        File modelFile = null;
        File testFile = null;
        File outFile = null;
        try {
             trainFile = File.createTempFile("train", ".txt");
             testFile = File.createTempFile("test", ".txt");
             modelFile = File.createTempFile("model", ".mdl");
             outFile = File.createTempFile("out", ".out");
             System.out.println(trainFile.getAbsolutePath());
             printTrainData(trainFile, trainLabels, trainData);
             printTrainData(testFile, testLabels, testData);
             trainCommandList.add(trainFile.getAbsolutePath());
             trainCommandList.add(modelFile.getAbsolutePath());
             
             testCommandList.add(testFile.getAbsolutePath());
             testCommandList.add(modelFile.getAbsolutePath());
             testCommandList.add(outFile.getAbsolutePath());
             
             String[] trainCommands = new String[trainCommandList.size()];
             trainCommands = trainCommandList.toArray(trainCommands);
             Process proc = rt.exec(trainCommands);
             
             BufferedReader stdInput = new BufferedReader(new 
                     InputStreamReader(proc.getInputStream()));
                
            String s = null;
            System.out.println("Output");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
             }
             
             String[] testCommands = new String[testCommandList.size()];
             testCommands = testCommandList.toArray(testCommands);
             proc = rt.exec(testCommands);

             stdInput = new BufferedReader(new 
                  InputStreamReader(proc.getInputStream()));
             
             double accuracy = -1.0;
             System.out.println("Output");
             while ((s = stdInput.readLine()) != null) {
                 
                 System.out.println(s);
                 if (s.startsWith("Cross Validation")) {
                     String accuracyString = s.split(" ")[4];
                     accuracy = Double.parseDouble(accuracyString.substring(0, accuracyString.length() - 1));
                 }
             }
             if (accuracy >= 0) {
                 return accuracy;
             }
             BufferedReader stdError = new BufferedReader(new 
                     InputStreamReader(proc.getErrorStream()));

             // read any errors from the attempted command
             System.out.println("Here is the standard error of the command (if any):\n");
             while ((s = stdError.readLine()) != null) {
                 System.out.println(s);
             }
             throw new UnimplementedException("");
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnimplementedException("");
        }
    }
    
    protected void printTrainData(File trainFile, String[] labels, double[][] features) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(trainFile));
        for (int i = 0; i < labels.length; i++) {
            writer.write(labels[i] + " ");
            printVector(writer, features[i], 1);
            writer.write("\n");
        }
        writer.close();
    }
    
    protected void printVector(BufferedWriter writer, double[] vector, int featureIndex) throws IOException {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < vector.length; i++) {
            buffer.append(i + featureIndex);
            buffer.append(":");
            buffer.append(vector[i]);
            if (i != vector.length - 1) {
                buffer.append(" ");
            }
        }
        writer.write(buffer.toString());
    }
}
