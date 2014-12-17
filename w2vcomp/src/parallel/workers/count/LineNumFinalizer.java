package parallel.workers.count;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterFinalizer;

public class LineNumFinalizer implements ParameterFinalizer{
    
    @Override
    public void finish(ModelParameters finalParameters) {
        System.out.println("Total number of lines:"
                + ((LineNumParameters) finalParameters).getValue());
    }

}
