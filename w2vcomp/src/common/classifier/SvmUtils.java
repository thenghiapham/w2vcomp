package common.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import common.exception.UnimplementedException;

public class SvmUtils {
    String svmDir;
    String[] options = {"-s","-t", "-d", "-c", "-g", "-p", "-v"} ;
    boolean[] intOption = {true, true, true, true, false, false, true};
    public SvmUtils(String svmDir) {
        this.svmDir = svmDir;
    }
    
    public void train(String trainFile, String modelFile, HashMap<String, Double> parameters) throws IOException{
        Runtime rt = Runtime.getRuntime();
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add(svmDir + "/svm-train");
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            if (parameters.containsKey(option)) {
                commandList.add(option);
                if (intOption[i]) {
                    commandList.add("" + Math.round(parameters.get(option)));
                } else {
                    commandList.add("" + parameters.get(option));
                }
            }
            
        }
        commandList.add(trainFile);
        commandList.add(modelFile);
        String[] commands = new String[commandList.size()];
        commands = commandList.toArray(commands);
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getInputStream()));
        
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        BufferedReader stdError = new BufferedReader(new 
                InputStreamReader(proc.getErrorStream()));

        System.out.println("Error:");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
    
    public double accuracy(String testFile, String modelFile, String outFile) throws IOException{
        Runtime rt = Runtime.getRuntime();
        if (outFile == null) {
            File temp = File.createTempFile("temp-file-name", ".tmp");
            outFile = temp.getAbsolutePath();
        }
        String[] commands = {svmDir + "/svm-predict",testFile, modelFile, outFile};
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getInputStream()));
        
        String s = null;
        double accuracy = -1.0;
        while ((s = stdInput.readLine()) != null) {
            if (s.startsWith("Accuracy")) {
                String accuracyString = s.split(" ")[2];
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
    }
    
    public static void main(String[] args) throws IOException{
        String svmDir = "/home/thenghiapham/Downloads/libsvm-3.20";
        SvmUtils utils = new SvmUtils(svmDir);
        
        String trainFile = "/home/thenghiapham/work/project/mikolov/imdb/svm/train.txt";
        String testFile = "/home/thenghiapham/work/project/mikolov/imdb/svm/test.txt";
        String modelFile = "/home/thenghiapham/Downloads/libsvm-3.20/add.mdl";
        HashMap<String, Double> parameters = new HashMap<String, Double>();
        parameters.put("-s", 0.0);
        parameters.put("-t", 2.0);
        parameters.put("-d", 2.0);
        utils.train(trainFile, modelFile, parameters);
        System.out.println(utils.accuracy(testFile, modelFile, null));
    }
}
