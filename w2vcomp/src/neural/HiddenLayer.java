package neural;

import org.ejml.simple.SimpleMatrix;

public class HiddenLayer {
    protected SimpleMatrix inputWeights;
    protected ActivationFunction activation;
    
    public HiddenLayer(SimpleMatrix inputWeight, ActivationFunction activation) {
        this.inputWeights = inputWeight;
        this.activation = activation;
    }
    
    public SimpleMatrix forward(SimpleMatrix input) {
        return null;
    }
}
