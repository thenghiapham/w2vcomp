package parallel.workers;


public interface ParameterAggregator {

    public ModelParameters aggregate(ModelParameters content);
    public ModelParameters getAggregatedParameters();

}
