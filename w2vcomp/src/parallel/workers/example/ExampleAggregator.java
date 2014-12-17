package parallel.workers.example;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterAggregator;

public class ExampleAggregator implements ParameterAggregator {

    ModelParameters params;

    public ExampleAggregator() {
        params = new ExampleModelParameters();
        // Initialization
        ((ExampleModelParameters) params).setValue("");
    }

    @Override
    public ModelParameters aggregate(Integer source, ModelParameters _content) {
        ExampleModelParameters content = 
                (ExampleModelParameters) _content;
        ((ExampleModelParameters) params).setValue(((ExampleModelParameters) params).getValue() + content.getValue());
        return params;
    }

    @Override
    public ModelParameters getInitParameters(Integer source) {
        return params;
    }
    

    public void finalize() {
    
    }
    
    @Override
    public void finalizeWorker(Integer source) {
        // TODO Auto-generated method stub
        
    }

}
