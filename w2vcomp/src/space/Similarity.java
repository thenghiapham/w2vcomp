package space;

import common.MathUtils;

public class Similarity {
    public static double cosine(double[] v1, double[] v2) {
        return MathUtils.cosine(v1, v2);
    }
    
    public static double dot(double[] v1, double[] v2) {
        return MathUtils.dot(v1, v2);
    }
}
