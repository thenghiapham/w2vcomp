package parallel.workers.example;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterFinalizer;

public class ExampleFinalizer implements ParameterFinalizer {

    @Override
    public void finish(ModelParameters finalParameters) {
        System.out.println("Final result:"
                + ((ExampleModelParameters) finalParameters).getValue());
    }

}
