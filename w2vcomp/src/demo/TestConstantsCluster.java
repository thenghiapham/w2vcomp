package demo;

public class TestConstantsCluster {
    
    public static final int wordDimensions = 300;
    public static final int imageDimensions = 300;
    public static final String typeOfLearning = "";

    public static final double rate_multiplier_grad = 1;
    public static final double rate_multiplier_sft = 1;

    public static final double lambda = 0.0001;
    public static final int negative_samples = 5;
    public static final double threshold = 3;
    public static final double margin = 0.5;




    public static final String TRAIN_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/corpora/enwik9";
    public static final String VISION_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/visualVectors/pmisvd.dm.aggr";
    public static final long SEED                       = 292626718599866L;
    public static final String TRAIN_CONCEPTS              = "/home/angeliki/Documents/mikolov_composition/misc/trainingConcepts.txt";                                     
    public static final String VECTOR_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/out_enwiki9"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                        +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                        "_r2"+rate_multiplier_grad+"l"+lambda+".bin";
    public static final String MAPPING_FUNCTION         = "/home/angeliki.lazaridou/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/out_enwiki9"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                        +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                        "_r2"+rate_multiplier_grad+"l"+lambda+".mapping";   
    public static final String VOCABULARY_FILE          = "/home/angeliki.lazaridou/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/out_enwik9"+typeOfLearning+".voc";
    public static final String INITIALIZATION_FILE      = "/home/angeliki.lazaridou/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/out_enwik9"+typeOfLearning+".ini";
    public static final String IMAGE_INITIALIZATION_FILE = "/home/angeliki.lazaridou/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/"+wordDimensions+"_"+imageDimensions+".ini";

    
    public static final String LOG_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/out_enwiki9"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                        +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                        "_r2"+rate_multiplier_grad+"l"+lambda+".log";
    public static final String LOG_DIR               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/";
    
    
    public static final String MEN_FILE                 = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
    public static final String SIMLEX_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/simlex-999";
    public static final String Carina_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/similarity_judgements3.txt";

    
    public static final String CCG_MEN_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
   
}
