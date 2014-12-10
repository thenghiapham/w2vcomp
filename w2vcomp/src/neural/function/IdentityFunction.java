package neural.function;


public class IdentityFunction implements ActivationFunction {

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

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "identity";
    }

}
