package space;

import org.ejml.simple.SimpleMatrix;

public interface CompositionalSemanticSpace extends SemanticSpace{
    public SimpleMatrix getComposedVector(String parseTreeString);
    public SimpleMatrix getComposedMatrix(String[] parseStrings);
}
