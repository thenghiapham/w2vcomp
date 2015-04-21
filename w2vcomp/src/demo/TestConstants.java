package demo;

public class TestConstants {
    
        
        public static final int wordDimensions = 100;
        public static final int imageDimensions = 100;
    
        public static final double rate_multiplier_grad = 1; //20 for mapping, 1 for mm
        public static final double rate_multiplier_sft = 10;
    
        public static final double lambda = 0.0001;  // 0.0001: add to zeros for less reg
        public static final int negative_samples = 40; // 5 for mapping, 20 for mm
        public static final double threshold = 5;
        public static final double margin = 0.5;
    
        public static final long SEED                       = 292626718599866L;

        public static final String typeOfLearning = "ts";

        
        public static final String ROOT_EXP_DIR                     = "/home/angeliki/Documents/cross-situational/";
        public static final String ROOT_EVAL_DIR        = "/home/angeliki/Documents/mikolov_composition/misc/";
        public static final String ROOT_VISUAL_DIR        = "/home/angeliki/sas/visLang/cross-situational/visual_symbols/all/";


        //public static final String ROOT_EXP_DIR                     = "/home/aggeliki/sas/visLang/cross-situational/";
        //public static final String ROOT_EVAL_DIR        = "/home/aggeliki/sas/visLang/cross-situational/misc/";
        //public static final String ROOT_VISUAL_DIR        = "/home/aggeliki/sas/visLang/cross-situational/visual_symbols/all/";
        
        //for pc
        
        //parallel data
        public static final String SOURCE_FILE                  = ROOT_EXP_DIR+"corpus/extended_version/cds.11_24.words.txt";
        //public static final String SOURCE_FILE                  = ROOT_EXP_DIR+"corpus/extended_version/words.txt";
        public static final String TARGET_FILE                  = ROOT_EXP_DIR+"corpus/extended_version/cds.11_24.images.txt";
        //public static final String TARGET_FILE                  = ROOT_EXP_DIR+"corpus/extended_version/objects.txt";
        public static final String TRAIN_DIR               = "";


        //visual vectors
        public static final String VISION_FILE                = ROOT_VISUAL_DIR+"fc7.txt";

        public static final String TRAIN_CONCEPTS              = "";   
        
        //model files
        public static final String MODEL_FILE               = null;
        
        public static final String VECTOR_FILE              =  ROOT_EXP_DIR+"experiments/vectors/"+typeOfLearning+"_d_"+wordDimensions+"_n"+negative_samples+"_m"+margin
                                                                                                      +"_r1"+rate_multiplier_sft
                                                                                                      +"_r2"+rate_multiplier_grad+"l"+lambda+".bin";                                                                                                                                                            
        public static final String MAPPING_FUNCTION         = ROOT_EXP_DIR+"experiments/vectors/"+typeOfLearning+"_d_"+wordDimensions+"_n"+negative_samples+"_m"+margin
                                                                                                        +"_r1"+rate_multiplier_sft
                                                                                                        +"_r2"+rate_multiplier_grad+"l"+lambda+".mapping"; 
        
        
        public static final String VOCABULARY_FILE_lang1          = ROOT_EXP_DIR+"experiments/vocab/"+typeOfLearning+".lang1.voc";
        public static final String VOCABULARY_FILE_lang2          = ROOT_EXP_DIR+"experiments/vocab/"+typeOfLearning+".lang2.voc";
        public static final String INITIALIZATION_FILE      = ROOT_EXP_DIR+"experiments/init/"+typeOfLearning+".ini";
        public static final String IMAGE_INITIALIZATION_FILE = ROOT_EXP_DIR+"experiments/init/"+typeOfLearning+".im"+TestConstants.wordDimensions+"_"+TestConstants.imageDimensions+".ini";
    
        public static final String LOG_FILE               = ROOT_EXP_DIR+"experiments/logs/dump.log";
        public static final String LOG_DIR               = ROOT_EXP_DIR+"experiments/logs";
        
        
        //eval files
        public static final String MEN_FILE             = ROOT_EVAL_DIR+"MEN_dataset_lemma_nopos_form_full";
        public static final String SIMLEX_FILE          = ROOT_EVAL_DIR+"simlex-999.full";
        public static final String Carina_FILE          = ROOT_EVAL_DIR+"similarity_judgements3.txt";
        public static final String CCG_MEN_FILE         = ROOT_EVAL_DIR+"MEN_dataset_lemma_nopos_form_full";
        public static final String CCG_AN_FILE          = ROOT_EVAL_DIR+"an_ml/an_ml_lemmapos.txt";

  
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
