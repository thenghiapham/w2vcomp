package parallel.workers.count;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import parallel.comm.ParameterMessager;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterEstimator;

public class LineNumEstimator implements ParameterEstimator {

//    protected BasicTreeInputStream inputStream;
    protected BufferedReader reader;
    
    public LineNumEstimator(String inputFile) {
        try {
//            inputStream = new BasicTreeInputStream(inputFile);
            reader = new BufferedReader(new FileReader(inputFile));
            System.out.println("File name: " + inputFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
//            inputStream = null;
            reader = null;
            e.printStackTrace();
        }
    }
    @Override
    public void run(Integer worker_id, ModelParameters init, ParameterMessager parameterMessager) {
        // TODO Auto-generated method stub
        long numLine = 0;
        LineNumParameters parameters = new LineNumParameters();
        try {
//            Tree tree = inputStream.readTree();
            String line = reader.readLine();
//            while (tree != null) {
            while (line != null) {
                numLine += 1;
                if (numLine % 10000 == 0) {
                    parameters.setValue(new Long(10000));
                    parameterMessager.sendUpdate(worker_id, parameters);
                    System.out.println("numLine: " + numLine);
                }
//                tree = inputStream.readTree();
                line = reader.readLine();
            }
            parameters.setValue(new Long(numLine % 10000));
            parameterMessager.sendUpdate(worker_id, parameters);
            System.out.println("numLine: " + numLine);
//            parameterMessager.sendEnd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
