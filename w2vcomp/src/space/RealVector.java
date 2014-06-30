package space;

public class RealVector {
    public static float[] subtract(float[] v1, float[] v2) {
        float[] result = new float[v1.length];
        for (int i = 0; i < v1.length; i++) {
            result[i] = v1[i] - v2[i];
        }
        return result;
    }

    public static float[] add(float[] v1, float[] v2) {
        float[] result = new float[v1.length];
        for (int i = 0; i < v1.length; i++) {
            result[i] = v1[i] + v2[i];
        }
        return result;
    }

    public static float[] norm(float[] v1) {
        float length = 0;
        float[] result = new float[v1.length];
        for (int i = 0; i < v1.length; i++) {
            length += v1[i] * v1[i];
        }
        length = (float) Math.sqrt(length);
        if (length != 0) {
            for (int i = 0; i < v1.length; i++) {
                result[i] += v1[i] / length;
            }
        }
        return result;
    }
}
