package parallel.workers.w2v;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterFinalizer;

public class W2vFinalizer implements ParameterFinalizer{
    
    @Override
    public void finish(ModelParameters finalParameters) {
        System.out.println("Total number of lines:"
                + finalParameters.getValue());
    }

}
