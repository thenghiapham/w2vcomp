package neural;

import org.ejml.simple.SimpleMatrix;

public abstract class LearningStrategy {
    public abstract SimpleMatrix getOutputWeights(String word);
    public abstract SimpleMatrix getGoldOutput(String word);
}
