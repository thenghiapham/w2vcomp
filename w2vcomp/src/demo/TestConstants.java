package demo;

public class TestConstants {
    
        
        public static final int wordDimensions = 300;
        public static final int imageDimensions = 300;
        public static final String typeOfLearning = "";
    
        public static final double rate_multiplier_grad = 1;
        public static final double rate_multiplier_sft = 1;
    
        public static final double lambda = 0.000001;  //add to zeros for less reg
        public static final int negative_samples = 5;
        public static final double threshold = 5;
        public static final double margin = 0.5;
    
    
        
        //for pc
//        public static final String TRAIN_FILE               = "/home/angeliki/Documents/mikolov_composition/corpora_simple/enwik9";
//        public static final String VISION_FILE              = "/home/angeliki/Documents/mikolov_composition/misc/pmisvd_more.aggr.dm";
//        public static final long SEED                       = 292626718599866L;
//        public static final String TRAIN_CONCEPTS              = "/home/angeliki/Documents/mikolov_composition/misc/trainingConcepts.txt";                                     
//        public static final String VECTOR_FILE              = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping_max/out_enwiki9"+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".bin";
//        public static final String MAPPING_FUNCTION         = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping_max/out_enwiki9"+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".mapping";   
//        public static final String VOCABULARY_FILE          = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic/out_enwik9"+typeOfLearning+".voc";
//        public static final String INITIALIZATION_FILE      = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic/out_enwik9"+typeOfLearning+".ini";
//        public static final String IMAGE_INITIALIZATION_FILE = "/home/angeliki/Documents/mikolov_composition/out/multimodal/hierarchical_stochastic_mapping_cosine/"+TestConstants.wordDimensions+"_"+TestConstants.imageDimensions+".ini";
//    
//        public static final String LOG_FILE               = "/home/angeliki/workspace/w2vcomp/w2vcomp/logs/dump.log";
//        public static final String LOG_DIR               = "/home/angeliki/workspace/w2vcomp/w2vcomp/logs";
//        
//        public static final String TRAIN_DIR               = "";
//        public static final String MEN_FILE             = "/home/angeliki/Documents/mikolov_composition/misc/MEN_dataset_lemma_nopos_form_full";
//        
//              
//        
//        public static final String SIMLEX_FILE             = "/home/pham/work/project/multimodal/misc/simlex-999";
//        public static final String Carina_FILE             = "/home/pham/work/project/multimodal/misc/similarity_judgements3.txt";
//        public static final String CCG_MEN_FILE             = "/home/pham/work/project/multimodal/misc/MEN_dataset_lemma_nopos_form_full";
//        public static final String CCG_AN_FILE              = "/home/thenghiapham/work/project/mikolov/an_ml/an_ml_lemmapos.txt";
//    
//
//        //for cluster
        public static final String TRAIN_DIR = "";
        public static final String TRAIN_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/corpora/wiki";
        public static final String VISION_FILE              = "/mnt/cimec-storage-sata/users/angeliki.lazaridou/mmskipgram/visualVectors/cnn.features_more.aggr.txt";
        public static final long SEED                       = 292626718599866L;
        public static final String TRAIN_CONCEPTS              = "/home/angeliki/Documents/mikolov_composition/misc/trainingConcepts.txt";                                     
        public static final String VECTOR_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/out_wiki"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".bin";
        public static final String MAPPING_FUNCTION         = "/home/angeliki.lazaridou/visLang/mmskipgram/out/hierarchical_stochastic_max_margin/out_wiki"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".mapping";   
        public static final String VOCABULARY_FILE          = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/out_wiki"+typeOfLearning+".voc";
        public static final String INITIALIZATION_FILE      = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/out_wiki"+typeOfLearning+".ini";
        public static final String IMAGE_INITIALIZATION_FILE = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/"+wordDimensions+"_"+imageDimensions+".ini";

        
        public static final String LOG_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/out_wiki"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".log";
        public static final String LOG_DIR               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/";
        
        
        public static final String MEN_FILE                 = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
        public static final String SIMLEX_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/simlex-999";
        public static final String Carina_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/similarity_judgements3.txt";

        
        public static final String CCG_MEN_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
       

}
