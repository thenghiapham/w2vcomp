package parallel.workers.example;

import java.io.Serializable;

import parallel.workers.ModelParameters;

public class ExampleModelParameters implements ModelParameters {
    private static final long serialVersionUID = 1L;
    
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = (String) value;
    }
    
}
