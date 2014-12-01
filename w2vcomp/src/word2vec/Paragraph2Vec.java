package word2vec;

import io.sentence.SentenceInputStream;

import java.util.ArrayList;

import common.exception.ValueException;

public class Paragraph2Vec extends AbstractWord2Vec{
    protected double[][] paragraphVectors;
    protected int iteration;
    
    public Paragraph2Vec(String networkFile, int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, int iteration) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample);
        loadNetwork(networkFile, true);
        this.iteration = iteration;
    }

    public void trainParagraphVector(String[] sentences) {
        
        
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // TODO Auto-generated method stub
        throw new ValueException("Not implemented");
        
    }
    
}
