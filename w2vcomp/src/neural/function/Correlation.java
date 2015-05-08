package neural.function;

public class Correlation {
    double[] predicted;
    double[] gold;
//    public train
    public Correlation(double[] predicted, double[] gold) {
        this.predicted = predicted;
        this.gold = gold;
    }
    public double[] derivative() {
        for (int i = 0; i < gold.length; i++) {
            
        }
        return null;
    }
}
