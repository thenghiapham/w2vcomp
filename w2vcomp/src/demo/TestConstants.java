package demo;

public class TestConstants {
    
        
        public static final int wordDimensions = 300;
        public static final int imageDimensions = 300;
        public static final String typeOfLearning = "";
    
        public static final double rate_multiplier_grad =20; //20 for mapping, 1 for mm
        public static final double rate_multiplier_sft = 1;
    
        public static final double lambda = 0.0001;  // 0.0001: add to zeros for less reg
        public static final int negative_samples = 5; // 5 for mapping, 20 for mm
        public static final double threshold = 5;
        public static final double margin = 0.5;
    
    
        
        //for pc
        public static final String TRAIN_FILE               = "/home/angeliki/Documents/mikolov_composition/corpora_simple/enwik9";
        //public static final String VISION_FILE              = "/home/angeliki/Documents/mikolov_composition/misc/pmisvd_more.aggr.dm";
        public static final String VISION_FILE                = "/home/angeliki/sata/mmskipgram/visualVectors/cnn.features_more.aggr.txt";

        public static final long SEED                       = 292626718599866L;
        public static final String TRAIN_CONCEPTS              = "/home/angeliki/Documents/mikolov_composition/misc/trainingConcepts_more.txt";      
        public static final String MODEL_FILE               = null;
        public static final String VECTOR_FILE              = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/mm_models/out_enwiki9"+typeOfLearning
                                                                                                                    +"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                                    +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                                    "_r2"+rate_multiplier_grad+"l"+lambda+".bin";                                                                                                                                                            
        public static final String MAPPING_FUNCTION         = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/mm_models/out_enwiki9"+typeOfLearning
                                                                                                                        +"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
                                                                                                                        +negative_samples+"_r1"+rate_multiplier_sft+
                                                                                                                        "_r2"+rate_multiplier_grad+"l"+lambda+".mapping"; 
        
        
        public static final String VOCABULARY_FILE          = "/home/angeliki/Documents/mikolov_composition/out/multimodal/ini/out_wiki"+typeOfLearning+".voc";
        public static final String INITIALIZATION_FILE      = "/home/angeliki/Documents/mikolov_composition/out/multimodal/ini/out_wiki"+typeOfLearning+".ini";
        public static final String IMAGE_INITIALIZATION_FILE = "/home/angeliki/Documents/mikolov_composition/out/multimodal/ini/"+TestConstants.wordDimensions+"_"+TestConstants.imageDimensions+".ini";
    
        public static final String LOG_FILE               = "/home/angeliki/workspace/w2vcomp/w2vcomp/logs_NAACL/dump.log";
        public static final String LOG_DIR               = "/home/angeliki/workspace/w2vcomp/w2vcomp/logs_NAACL";
        
        public static final String TRAIN_DIR               = "";
        public static final String MEN_FILE             = "/home/angeliki/Documents/mikolov_composition/misc/MEN_dataset_lemma_nopos_form_full";
        
              
        
        public static final String SIMLEX_FILE             = "/home/angeliki/Documents/mikolov_composition/misc/simlex-999.full";
        public static final String Carina_FILE             = "/home/angeliki/Documents/mikolov_composition/misc/similarity_judgements3.txt";
        public static final String CCG_MEN_FILE             = "/home/angeliki/Documents/mikolov_composition/misc/MEN_dataset_lemma_nopos_form_full";
        public static final String CCG_AN_FILE              = "/home/thenghiapham/work/project/mikolov/an_ml/an_ml_lemmapos.txt";
//
        
////        
//        //for cluster
//        public static final String TRAIN_DIR                 = "/home/angeliki.lazaridou/visLang/mmskipgram/corpora/multi_3b/";
//        public static final String TRAIN_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/corpora/wiki";
//        public static final String VISION_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/visualVectors/cnn_svd.dm.aggr";
//        //public static final String VISION_FILE                = "/mnt/cimec-storage-sata/users/angeliki.lazaridou/mmskipgram/visualVectors/cnn.features_more.aggr.txt";
//
//        public static final long SEED                       = 292626718599866L;
//        public static final String TRAIN_CONCEPTS              = "/home/angeliki.lazaridou/visLang/mmskipgram/visualVectors/trainingConcepts_more.txt";      
//        
//        
//        public static final String VECTOR_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".bin";
//        public static final String MODEL_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                             +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                                 "_r2"+rate_multiplier_grad+"l"+lambda+".hs";
//        public static final String MAPPING_FUNCTION         = "/home/angeliki.lazaridou/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".mapping";   
//        
//        public static final String LOG_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/3b"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".log";
//        public static final String LOG_DIR               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/";
//
//        
//        
//        
//        //public static final String VOCABULARY_FILE          = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/out_3b"+typeOfLearning+".voc";
//        public static final String VOCABULARY_FILE          = "/home/angeliki/sas/visLang/mmskipgram/ini/out_3b"+typeOfLearning+".voc";
//        public static final String INITIALIZATION_FILE      = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/out_3b"+typeOfLearning+".ini";
//        public static final String IMAGE_INITIALIZATION_FILE = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/"+wordDimensions+"_"+imageDimensions+".ini";
//
//        
//       
//        
//        
//        public static final String MEN_FILE                 = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
//        public static final String SIMLEX_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/simlex-999";
//        public static final String Carina_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/similarity_judgements3.txt";
//
//        
//        public static final String CCG_MEN_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
////        

}
