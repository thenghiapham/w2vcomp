package parallel.workers.w2v;

import java.io.IOException;


import io.sentence.BasicTreeInputStream;
import parallel.comm.ParameterMessager;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterEstimator;
import tree.Tree;

public class W2vEstimator implements ParameterEstimator {

    protected BasicTreeInputStream inputStream;
    
    public W2vEstimator(String inputFile) {
        try {
            inputStream = new BasicTreeInputStream(inputFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            inputStream = null;
            e.printStackTrace();
        }
    }
    @Override
    public void run(ModelParameters init, ParameterMessager parameterMessager) {
        // TODO Auto-generated method stub
        long numLine = 0;
        W2vParameters parameters = new W2vParameters();
        try {
            Tree tree = inputStream.readTree();
            
            while (tree != null) {
                numLine += 1;
                if (numLine == 100000) {
                    parameters.setValue(numLine);
                    parameterMessager.sendUpdate(parameters);
                    numLine = 0;
                }
                tree = inputStream.readTree();
            }
            parameters.setValue(numLine);
            parameterMessager.sendUpdate(parameters);
            parameterMessager.sendEnd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
