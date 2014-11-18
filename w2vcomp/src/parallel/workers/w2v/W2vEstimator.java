package parallel.workers.w2v;

import java.io.IOException;

import io.sentence.BasicTreeInputStream;
import parallel.comm.ParameterMessager;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterEstimator;

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
        while (line!)
    }
    

}
