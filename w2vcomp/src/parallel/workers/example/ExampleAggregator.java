package parallel.workers.example;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterAggregator;

public class ExampleAggregator implements ParameterAggregator {

    ModelParameters params;

    public ExampleAggregator() {
        params = new ExampleModelParameters();
        // Initialization
        params.setValue("");
    }

    @Override
    public ModelParameters aggregate(Integer source, ModelParameters _content) {
        ExampleModelParameters content = 
                (ExampleModelParameters) _content;
        params.setValue(params.getValue() + content.getValue());
        return params;
    }

    @Override
    public ModelParameters getInitParameters(Integer source) {
        return params;
    }
    
    @Override
    public ModelParameters getFinalParameters(Integer source) {
        return params;
    }

}
