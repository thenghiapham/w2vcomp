package common.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import common.exception.UnimplementedException;

public class SvmCrossValidation {
    String svmDir;
    public SvmCrossValidation(String svmDir) {
        this.svmDir = svmDir; 
    }
    public double crossValidation(String[] labels, double[][] features, int numFolds, String optionString) {
        Runtime rt = Runtime.getRuntime();
        ArrayList<String> commandList = new ArrayList<String>();
        
        commandList.add(svmDir + "/svm-train");
        if (optionString.length() > 0)
        {
            String[] options = optionString.split(" ");
            for (String option: options) {
                commandList.add(option);
            }
        }
        commandList.add("-v");
        commandList.add("" + numFolds);
        File trainFile = null;
        try {
             trainFile = File.createTempFile("train", ".txt");
             System.out.println(trainFile.getAbsolutePath());
             printTrainData(trainFile, labels, features);
             commandList.add(trainFile.getAbsolutePath());
             String[] commands = new String[commandList.size()];
             commands = commandList.toArray(commands);
             Process proc = rt.exec(commands);

             BufferedReader stdInput = new BufferedReader(new 
                  InputStreamReader(proc.getInputStream()));
             
             String s = null;
             double accuracy = -1.0;
//             System.out.println("Output");
             while ((s = stdInput.readLine()) != null) {
                 
//                 System.out.println(s);
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
