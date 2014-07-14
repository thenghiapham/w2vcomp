package common;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

public class ValueGradient {
    public double value;
    public ArrayList<SimpleMatrix> gradients;
    
    public ValueGradient(double value, ArrayList<SimpleMatrix> gradients) {
        this.value = value;
        this.gradients = gradients;
    }
    
}
