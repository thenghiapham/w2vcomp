package common.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import common.exception.UnimplementedException;

public class SvmUtils {
    String svmDir;
    public SvmUtils(String svmDir) {
        this.svmDir = svmDir;
    }
    
    public double accuracy(String testFile, String modelFile, String outFile) throws IOException{
        Runtime rt = Runtime.getRuntime();
        if (outFile == null) {
            File temp = File.createTempFile("temp-file-name", ".tmp");
            outFile = temp.getAbsolutePath();
        }
        String[] commands = {svmDir + "/svm-predict","-s", "0",testFile, modelFile, outFile};
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getInputStream()));
        
        String s = null;
        double accuracy = -1.0;
        while ((s = stdInput.readLine()) != null) {
            if (s.startsWith("Accuracy")) {
                accuracy = Double.parseDouble(s.split(" ")[2]);
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
        String testFile = "/home/thenghiapham/work/project/mikolov/imdb/svm/test.txt";
        String modelFile = "/home/thenghiapham/Downloads/libsvm-3.20/add.mdl";
        System.out.println(utils.accuracy(testFile, modelFile, null));
    }
}
