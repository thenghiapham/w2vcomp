package parallel.workers;


public interface ParameterAggregator {

    public ModelParameters aggregate(Integer source, ModelParameters content);
    public ModelParameters getInitParameters(Integer source);
    public void finalize();
    public void finalizeWorker(Integer source);

}
