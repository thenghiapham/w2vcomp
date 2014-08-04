package neural;

import org.ejml.simple.SimpleMatrix;

public interface Layer {
    public void forward();
    public void backward();
    public void addInLayer(Layer inLayer);
    public void addOutLayer(Layer inLayer);
    public SimpleMatrix getOutput();
    public SimpleMatrix getError();
    public SimpleMatrix getGradient();
}
