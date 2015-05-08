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
        double aveX = 0;
        double aveY = 0;
        double aveX2 = 0;
        double aveY2 = 0;
        double aveXY =0;
        for (int i = 0; i < gold.length; i++) {
            aveX += predicted[i];
            aveX += predicted[i] * predicted[i];
        }
        return null;
    }
}

