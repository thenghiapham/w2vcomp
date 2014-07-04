package common;

public class SigmoidTable {
    
    public static final float DEFAULT_MAX_X              = 6;
    public static final int   DEFAULT_SIGMOID_TABLE_SIZE = 100000;

    private float[]           sigmoidTable;
    // TODO: does maxX has any importance other than this: e.g. cut off the
    // gradient, (maybe in the update not here)
    private float             maxX;
    private int               tableSize;


    /**
     * Constructor
     */
    public SigmoidTable(int tableSize, float maxX) {
        this.tableSize = tableSize;
        this.maxX = maxX;
        initTable();
    }

    /**
     * Default constructor
     * Initialize with default values
     */
    public SigmoidTable() {
        this(DEFAULT_SIGMOID_TABLE_SIZE, DEFAULT_MAX_X);
    }
    
    /**
     * Initialize the precomputed sigmoid table.
     * The table consists of "tableSize" precomputed values for sigmoid 
     * function for input values from -maxX to maxX (The difference between to
     * consecutive input value would be: 2 * maxX / (tableSize - 1)
     */
    public void initTable() {
        sigmoidTable = new float[tableSize];
        float step = (2 * maxX) / (tableSize - 1);
        for (int i = 0; i < tableSize - 1; i++) {
            float x = -maxX + i * step;
            sigmoidTable[i] = (float) MathUtils.sigmoid(x);
        }
    }

    /**
     * Get the sigmoid function for x from the precomputed table
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
