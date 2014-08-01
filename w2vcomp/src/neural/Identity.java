package neural;

public class Identity implements ActivationFunction {

    @Override
    public double activation(double x) {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public double derivative(double x) {
        // TODO Auto-generated method stub
        return 1;
    }

}
