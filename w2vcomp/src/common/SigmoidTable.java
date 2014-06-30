package common;

public class SigmoidTable {
    public static final float DEFAULT_MAX_X              = 6;
    public static final int   DEFAULT_SIGMOID_TABLE_SIZE = 100000;

    private float[]           sigmoidTable;
    private float             maxX;
    private int               tableSize;

    /*
     * Real sigmoid function Note: 1 / (1 + exp(-x)) = exp(x) / (1 + exp(x))
     */
    public static double sigmoid(double f) {
        double e_x = Math.exp(f);
        return e_x / (e_x + 1);
    }

    /*
     * initialize the precomputed sigmoid table The table consists of
     * "tableSize" precomputed values for sigmoid function for input values from
     * -maxX to maxX (The difference between to consecutive input value would be
     * 2 * maxX / (tableSize - 1)
     */
    public void initTable() {
        sigmoidTable = new float[tableSize];
        float step = (2 * maxX) / (tableSize - 1);
        for (int i = 0; i < tableSize - 1; i++) {
            float x = -maxX + i * step;
            sigmoidTable[i] = (float) sigmoid(x);
        }
    }

    /*
     * Constructor
     */
    public SigmoidTable(int tableSize, float maxX) {
        this.tableSize = tableSize;
        this.maxX = maxX;
        initTable();
    }

    public SigmoidTable() {
        this(DEFAULT_SIGMOID_TABLE_SIZE, DEFAULT_MAX_X);
    }

    /*
     * get the sigmoid function for x from the precomputed table
     */
    public float getSigmoid(float x) {
        if (x > maxX)
            return 1;
        else if (x < -maxX)
            return 0;
        else {
            int index = Math.round((x + maxX) / (2 * maxX) * (tableSize - 1));
            return sigmoidTable[index];
        }

    }

}
