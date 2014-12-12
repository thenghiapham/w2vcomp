package parallel.workers;


public interface ParameterAggregator {

    public ModelParameters aggregate(Integer source, ModelParameters content);
    public ModelParameters getInitParameters(Integer source);
    public ModelParameters getFinalParameters(Integer source);

}
