package common;

import java.util.List;

import neural.ActivationFunction;

import org.ejml.alg.dense.mult.MatrixDimensionException;
import org.ejml.simple.SimpleMatrix;

/**
 * This class provides some utility method for the SimpleMatrix class
 * @author thenghiapham
 *
 */
public class SimpleMatrixUtils {
    
    /**
     * Return a matrix which consist of the values of sigmoid function of every
     * element of the input matrix
     * The sigmoid values are taken from an instance of SigmoidTable class
     * @param inputMatrix
     * @param sigmoidTable
     * @return
     */
    public static SimpleMatrix elementwiseApplySigmoid(SimpleMatrix inputMatrix, SigmoidTable sigmoidTable) {
        double[][] matrix = new double[inputMatrix.numRows()][inputMatrix.numCols()];
        for (int i = 0; i < inputMatrix.numRows(); i++)
            for (int j = 0; j < inputMatrix.numCols(); j++) {
                matrix[i][j] = sigmoidTable.getSigmoid(inputMatrix.get(i,j));
            }
        return new SimpleMatrix(matrix);
    }
    
    /**
     * Stack two matrices vertically
     * @param matrix1
     * @param matrix2
     * @return
     * @throws MatrixDimensionException
     */
    public static SimpleMatrix vStack(SimpleMatrix matrix1, SimpleMatrix matrix2) throws MatrixDimensionException{
        if (matrix1.numCols() != matrix2.numCols()) {
            throw new MatrixDimensionException("Number of columns do not match");
        }
        int numCols = matrix1.numCols();
        int numRows1 = matrix1.numRows();
        int numRows2 = matrix2.numRows();
        double[] newData = new double[numCols * (numRows1 + numRows2)];
        System.arraycopy(matrix1.getMatrix().data, 0, newData, 0, numCols * numRows1);
        System.arraycopy(matrix2.getMatrix().data, 0, newData, numCols * numRows1, numCols * numRows2);
        return new SimpleMatrix(numRows1 + numRows2, numCols, true, newData);
    }
    
    /**
     * Stack two matrices horizontally
     * @param matrix1
     * @param matrix2
     * @return
     * @throws MatrixDimensionException
     */
    public static SimpleMatrix hStack(SimpleMatrix matrix1, SimpleMatrix matrix2) throws MatrixDimensionException{
        if (matrix1.numRows() != matrix2.numRows()) {
            throw new MatrixDimensionException("Number of rows do not match");
        }
        int numCols1 = matrix1.numCols();
        int numCols2 = matrix2.numCols();
        int numRows = matrix1.numRows();
        double[] newData = new double[numRows * (numCols1 + numCols2)];
        System.arraycopy(matrix1.transpose().getMatrix().data, 0, newData, 0, numRows * numCols1);
        System.arraycopy(matrix2.transpose().getMatrix().data, 0, newData, numRows * numCols1, numRows * numCols2);
        return new SimpleMatrix(numRows, numCols1 + numCols2, false, newData);
    }
    
    /**
     * Return the data of a SimpleMatrix as a 2d array
     * (Since the internal structure of a SimpleMatrix is a 1d array, not 2d) 
     * @param matrix
     * @return
     */
    public static double[][] to2DArray(SimpleMatrix matrix) {
        double[] oneDArray = matrix.getMatrix().data;
        int numRows = matrix.numRows();
        int numCols = matrix.numCols();
        double[][] result = new double[numRows][numCols];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(oneDArray, i * numCols, result[i], 0, numCols);
        }
        return result;
    }
    
    // TODO: turn this into a UnitTest
    public static void main(String[] args) {
        SimpleMatrix matrix1 = new SimpleMatrix(2,2,true, new double[]{1,2,3,4});
        SimpleMatrix matrix2 = new SimpleMatrix(2,2,false, new double[]{5,6,7,8});
        System.out.println("mat 1" + matrix1);
        System.out.println("mat 2" + matrix2);
        System.out.println("vstacked" + vStack(matrix1, matrix2));
        System.out.println("hstacked" + hStack(matrix1, matrix2));
    }
    
    public static SimpleMatrix applyActivationFunction(SimpleMatrix input, ActivationFunction activation) {
        double[] data = input.getMatrix().getData();
        double[] newData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            newData[i] = activation.activation(data[i]);
        }
        return new SimpleMatrix(input.numRows(), input.numCols(), true, newData);
    }
    
    public static SimpleMatrix applyDerivative(SimpleMatrix input, ActivationFunction activation) {
        double[] data = input.getMatrix().getData();
        double[] newData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            newData[i] = activation.derivative(data[i]);
        }
        return new SimpleMatrix(input.numRows(), input.numCols(), true, newData);
    }
    
    public static SimpleMatrix getRows(SimpleMatrix originalMatrix, int[] indices) {
        int numCols = originalMatrix.numCols();
        double[] originalData = originalMatrix.getMatrix().getData();
        double[] destinationData = new double[indices.length * numCols];
        for (int i = 0;i < indices.length; i++) {
            int index = indices[i];
            System.arraycopy(originalData, numCols * index, destinationData, 
                    i * numCols, numCols);
        }
        return new SimpleMatrix(indices.length, numCols, true, destinationData);
    }
    
    public static SimpleMatrix concatenateVectors(List<SimpleMatrix> vectors, boolean columnVector) {
        // TODO: check not concatenating matrices
        int length = 0;
        for (SimpleMatrix vector: vectors) length += vector.numRows();
        double[] newData = new double[length];
        int pos = 0; 
        for (SimpleMatrix vector: vectors) {
            int vectorLength = vector.numRows();
            System.arraycopy(vector.getMatrix().getData(), 0, newData, pos, vectorLength);
            pos += vectorLength;
        }
        return new SimpleMatrix(length,1,true,newData);
    }
    
}
