package demo;

public class TestConstants {
    
        
        public static int wordDimensions = 200;
        public static int imageDimensions = 200;
    

        public static double rate_multiplier_grad =50; //50 for mapping, 1 for mm
        public static double rate_multiplier_sft = 10; //10

    
        public static double lambda = 0.0001;  // 0.0001: add to zeros for less reg
        public static int negative_samples = 40; // 5 for mapping, 20 for mm
        public static double threshold = 5;
        public static double margin = 0.5;
    
        public static long SEED                       = 292626718599866L;

        public static String typeOfLearning = "frank";


        /*
        public static String ROOT_EXP_DIR                     = "/home/angeliki/Documents/cross-situational/";
        public static String ROOT_EVAL_DIR        = "/home/angeliki/Documents/mikolov_composition/misc/";
        public static String ROOT_VISUAL_DIR        = "/home/angeliki/sas/visLang/cross-situational/visual_symbols/all/";
         */
        public static String ROOT_EXP_DIR                     = "/home/aggeliki/visLang/cross-situational/";
        public static String ROOT_EVAL_DIR        = "/home/aggeliki/visLang/cross-situational/misc/";
        public static String ROOT_VISUAL_DIR        = "/home/aggeliki/visLang/cross-situational/visual_symbols/all/";

        
        //for pc
        
        //parallel data
        //public static String SOURCE_FILE_TRAIN                  = ROOT_EXP_DIR+"corpus/extended_version/cds.11_24.words.txt";
        //public static String SOURCE_FILE_TRAIN                    = ROOT_EXP_DIR+"corpus/extended_v2_version/words.txt";
        public static String SOURCE_FILE_TRAIN                = ROOT_EXP_DIR+"corpus/frank/words.txt";
        public static String SOURCE_FILE_TEST                  = ROOT_EXP_DIR+"corpus/frank/words.txt";
        //public static String TARGET_FILE_TRAIN                  = ROOT_EXP_DIR+"corpus/extended_version/cds.11_24.images.txt";
        //public static String TARGET_FILE_TRAIN                = ROOT_EXP_DIR+"corpus/extended_v2_version/objects.txt";
        public static String TARGET_FILE_TEST                  = ROOT_EXP_DIR+"corpus/frank/objects.txt.shuf";
        public static String TARGET_FILE_TRAIN                  = ROOT_EXP_DIR+"corpus/frank/objects_shuffle_order.txt";

        public static String TRAIN_DIR               = "";


        //visual vectors
        public static String VISION_FILE                = ROOT_VISUAL_DIR+"fc7.txt";
        //public static String VISION_FILE                = ROOT_VISUAL_DIR+"fc7_rand.txt";

        public static String TRAIN_CONCEPTS              = "";   
        
        //model files
        public static String MODEL_FILE               = null;
        
        public static String VECTOR_FILE              =  ROOT_EXP_DIR+"experiments/vectors/"+typeOfLearning+"_d_"+wordDimensions+"_n"+negative_samples+"_m"+margin
                                                                                                      +"_r1"+rate_multiplier_sft
                                                                                                      +"_r2"+rate_multiplier_grad+"l"+lambda+".bin";                                                                                                                                                            
        public static String MAPPING_FUNCTION         = ROOT_EXP_DIR+"experiments/vectors/"+typeOfLearning+"_d_"+wordDimensions+"_n"+negative_samples+"_m"+margin
                                                                                                        +"_r1"+rate_multiplier_sft
                                                                                                        +"_r2"+rate_multiplier_grad+"l"+lambda+".mapping"; 
        
        
        public static String VOCABULARY_FILE_lang1          = ROOT_EXP_DIR+"experiments/vocab/"+typeOfLearning+".lang1.voc";
        public static String VOCABULARY_FILE_lang2          = ROOT_EXP_DIR+"experiments/vocab/"+typeOfLearning+".lang2.voc";
        public static String INITIALIZATION_FILE      = ROOT_EXP_DIR+"experiments/init/"+typeOfLearning+".ini";
        public static String IMAGE_INITIALIZATION_FILE = ROOT_EXP_DIR+"experiments/init/"+typeOfLearning+".im"+TestConstants.wordDimensions+"_"+TestConstants.imageDimensions+".ini";
    
        public static String LOG_FILE               = ROOT_EXP_DIR+"experiments/logs/dump.log";
        public static String LOG_DIR               = ROOT_EXP_DIR+"experiments/logs";
        
        
        //eval files
        public static String MEN_FILE             = ROOT_EVAL_DIR+"MEN_dataset_lemma_nopos_form_full";
        public static String SIMLEX_FILE          = ROOT_EVAL_DIR+"simlex-999.full";
        public static String Carina_FILE          = ROOT_EVAL_DIR+"similarity_judgements3.txt";
        public static String CCG_MEN_FILE         = ROOT_EVAL_DIR+"MEN_dataset_lemma_nopos_form_full";
        public static String CCG_AN_FILE          = ROOT_EVAL_DIR+"an_ml/an_ml_lemmapos.txt";

  
//        //for cluster
//        public static String TRAIN_DIR                 = "/home/angeliki.lazaridou/visLang/mmskipgram/corpora/multi_3b/";
//        public static String TRAIN_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/corpora/wiki";
//        public static String VISION_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/visualVectors/cnn_svd.dm.aggr";
//        //public static String VISION_FILE                = "/mnt/cimec-storage-sata/users/angeliki.lazaridou/mmskipgram/visualVectors/cnn.features_more.aggr.txt";
//
//        public static long SEED                       = 292626718599866L;
//        public static String TRAIN_CONCEPTS              = "/home/angeliki.lazaridou/visLang/mmskipgram/visualVectors/trainingConcepts_more.txt";      
//        
//        
//        public static String VECTOR_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".bin";
//        public static String MODEL_FILE              = "/home/angeliki.lazaridou/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                             +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                                 "_r2"+rate_multiplier_grad+"l"+lambda+".hs";
//        public static String MAPPING_FUNCTION         = "/home/angeliki.lazaridou/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".mapping";   
//        
//        public static String LOG_FILE               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/3b"+"_n"+negative_samples+"_m"+margin+typeOfLearning+"_"
//                                                                                                            +negative_samples+"_r1"+rate_multiplier_sft+
//                                                                                                            "_r2"+rate_multiplier_grad+"l"+lambda+".log";
//        public static String LOG_DIR               = "/home/angeliki.lazaridou/visLang/mmskipgram/logs/";
//
//        
//        
//        
//        //public static String VOCABULARY_FILE          = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/out_3b"+typeOfLearning+".voc";
//        public static String VOCABULARY_FILE          = "/home/angeliki/sas/visLang/mmskipgram/ini/out_3b"+typeOfLearning+".voc";
//        public static String INITIALIZATION_FILE      = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/out_3b"+typeOfLearning+".ini";
//        public static String IMAGE_INITIALIZATION_FILE = "/home/angeliki.lazaridou/visLang/mmskipgram/ini/"+wordDimensions+"_"+imageDimensions+".ini";
//
//        
//       
//        
//        
//        public static String MEN_FILE                 = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
//        public static String SIMLEX_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/simlex-999";
//        public static String Carina_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/similarity_judgements3.txt";
//
//        
//        public static String CCG_MEN_FILE             = "/home/angeliki.lazaridou/visLang/mmskipgram/misc/MEN_dataset_lemma_nopos_form_full";
////        

}
