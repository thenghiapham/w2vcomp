package space;

public class RealVector {
    public static double[] subtract(double[] v1, double[] v2) {
        double[] result = new double[v1.length];
        for (int i = 0; i < v1.length; i++) {
            result[i] = v1[i] - v2[i];
        }
        return result;
    }

    public static double[] add(double[] v1, double[] v2) {
        double[] result = new double[v1.length];
        for (int i = 0; i < v1.length; i++) {
            result[i] = v1[i] + v2[i];
        }
        return result;
    }

    public static double[] norm(double[] v1) {
        double length = 0;
        double[] result = new double[v1.length];
        for (int i = 0; i < v1.length; i++) {
            length += v1[i] * v1[i];
        }
        length = (double) Math.sqrt(length);
        if (length != 0) {
            for (int i = 0; i < v1.length; i++) {
                result[i] += v1[i] / length;
            }
        }
        return result;
    }
}
