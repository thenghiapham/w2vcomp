package common;

public class MathUtils {
    public static double cosine(float[] v1, float[] v2) {
        return dot(v1, v2) / (length(v1) * length(v2));
    }

    public static double length(float[] v1) {
        double norm = dot(v1, v1);
        return Math.sqrt(norm);
    }

    public static double dot(float[] v1, float[] v2) {
        double result = 0;
        for (int i = 0; i < v1.length; i++) {
            result += v1[i] * v2[i];
        }
        return result;
    }

    public static float exp_sigmoid(float f) {
        return 1 - (float) (1.0 / (1.0 + Math.exp(f)));
    }
}
