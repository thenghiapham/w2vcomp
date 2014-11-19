package parallel.workers.models;

import parallel.comm.ParameterMessager;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterEstimator;

public class ExampleEstimator implements ParameterEstimator {

    public ExampleEstimator(String trainingFile) {
        //Do nothing
    }

    @Override
    public void run(ModelParameters _init, ParameterMessager parameterMessager) {
        
        // model initalization 
        ModelParameters init = (ExampleModelParameters) _init;
        // We disregard in the example the initial parameters
        
        
        ExampleModelParameters aggregated = new ExampleModelParameters();
        for (int request_nbr = 0; request_nbr < 10; request_nbr++) {
            aggregated.setValue("" + request_nbr);
            aggregated = (ExampleModelParameters) parameterMessager
                    .sendUpdate(aggregated).getContent();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
