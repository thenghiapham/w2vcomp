package demo;

public class TestConstants {
    
    public static final boolean LOWER_CASE = true;
    
    public static final String TRAIN_FILE               = "/home/thenghiapham/work/project/mikolov/text/wikiA_sample.txt";
    public static final String TRAIN_DIR                = "/home/thenghiapham/work/project/mikolov/text/tmp";
    public static final String VECTOR_FILE              = "/home/thenghiapham/work/project/mikolov/text/wikiA.bin";
    public static final String VOCABULARY_FILE          = "/home/thenghiapham/work/project/mikolov/text/wikiA.voc";
    public static final String INITIALIZATION_FILE      = "/home/thenghiapham/work/project/mikolov/text/wikiA.ini";
    public static final String MATRIX_FILE              = "/home/thenghiapham/work/project/mikolov/text/wikiA.ini";
    
    
    public static final String S_PROJECT_DIR              = "/home/thenghiapham/work/project/mikolov/";
//  public static final String S_PROJECT_DIR              = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/";
    public static final String S_CONSTRUCTION_FILE        = S_PROJECT_DIR + "selected-constructions1.txt";
//  public static final String S_CONSTRUCTION_FILE        = S_PROJECT_DIR + "tmp-constructions.txt";
  public static final String S_TRAIN_DIR                = S_PROJECT_DIR + "split_parse/wiki";
//    public static final String S_TRAIN_DIR                = S_PROJECT_DIR + "split_parse/bnc";
//    public static final String S_TRAIN_DIR                = S_PROJECT_DIR + "tmp_parsed/bnc";
    public static final String S_TRAIN_FILE               = S_PROJECT_DIR + "tmp_parsed/bnc/bnc.txt";
//    public static final int S_MIN_FREQUENCY               = 5;
    public static final int S_MIN_FREQUENCY               = 100;
    public static final String S_LOG_DIR                  = S_PROJECT_DIR + "log/";
//    public static final String S_LOG_DIR                  = "/home/pham/work/project/compomik/log/";
//  public static final String S_LOG_FILE                 = S_LOG_DIR + "wiki_sentence.log";
    
    public static final String S_OUT_DIR                  = S_PROJECT_DIR + "output/";
//  public static final String S_VECTOR_FILE              = S_OUT_DIR + "wwiki.bin";
//  public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "wwiki.cmp";
//  public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "wwiki.voc";
//  public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "wwiki.ini";
//    public static final String S_VECTOR_FILE              = S_OUT_DIR + "dbnc300t.bin";
//    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "dbnc300t.cmp";
//    public static final String S_MODEL_FILE               = S_OUT_DIR + "dbnc300t.mdl";
//    public static final String S_LOG_FILE                 = S_LOG_DIR + "dbnc300t.log";
//    public static final String S_VECTOR_FILE              = S_OUT_DIR + "s_neg_rdubnc40ts.bin";
//    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "s_neg_rdubnc40ts.cmp";
//    public static final String S_MODEL_FILE               = S_OUT_DIR + "s_neg_rdubnc40ts.mdl";
//    public static final String S_LOG_FILE                 = S_LOG_DIR + "s_neg_rdubnc40ts.log";
//    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "bnc_lower_1.voc";
    
    public static final String S_VECTOR_FILE              = S_OUT_DIR + "s_neg_i4wlwiki100is_wo_lex.bin";
    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "s_neg_i4wlwiki100is_wo_lex.cmp";
    public static final String S_MODEL_FILE               = S_OUT_DIR + "s_neg_i4wlwiki100is_wo_lex.mdl";
    public static final String S_LOG_FILE                 = S_LOG_DIR + "s_neg_i4wlwiki100is_wo_lex.log";
    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "wiki_lower.voc";
    
    public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "skipgram.mdl";
//    public static final int S_MIN_FREQUENCY               = 10;
    
//    public static final String S_VECTOR_FILE              = S_OUT_DIR + "skip_bnc40.bin";
//    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "skip_bnc40.cmp";
//    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "skip_bnc100.voc";
//    public static final String S_MODEL_FILE               = S_OUT_DIR + "skipgram100.mdl";
//    public static final String S_WORD_VECTOR_FILE         = S_OUT_DIR + "hs_skip_wiki_size.bin";
//    public static final String S_WORD_MODEL_FILE          = S_OUT_DIR + "hs_skip_wiki_size.mdl";
//    public static final String S_WORD_LOG_FILE            = S_LOG_DIR + "hs_skip_wiki_size.log";
    
    public static final String S_WORD_VECTOR_FILE         = S_OUT_DIR + "neg_skip_bnc_ws_size.bin";
    public static final String S_WORD_MODEL_FILE          = S_OUT_DIR + "neg_skip_bnc_ws_size.mdl";
    public static final String S_WORD_LOG_FILE            = S_LOG_DIR + "neg_skip_bnc_ws_size.log";
    
//    public static final String S_VECTOR_FILE              = S_OUT_DIR + "skip_wiki300_ss.bin";
//    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "skip_wiki300_ss.cmp";
//    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "skip_wiki300_ss.voc";
//    public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "skip_wiki300_ss.ini";
    
//    public static final String S_VECTOR_FILE              = S_OUT_DIR + "cbow_bnc100.bin";
//    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "cbow_bnc100.cmp";
//    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "cbow_bnc300.voc";
//    public static final String S_MODEL_FILE               = S_OUT_DIR + "cbow_bnc100.mdl";
    
    
    public static final String S_RTE_DIR                  = S_PROJECT_DIR + "rte/";
    public static final String S_RTE_FILE                 = S_RTE_DIR + "SICK_train_trial.txt";
    public static final String S_RTE_FEATURE_FILE         = S_RTE_DIR + "feature/SICK_train_trial.txt";
    public static final String S_RTE_SVM_DIR              = S_PROJECT_DIR + "rte/svm/";
    
    public static final String S_IMDB_DIR                   = S_PROJECT_DIR + "imdb/";
    public static final String S_IMDB_FILE                 = S_IMDB_DIR + "normalized_text.parsed";
    public static final String S_IMDB_LABEL_FILE           = S_IMDB_DIR + "sentence_sentiment.txt2";
    public static final String S_IMDB_SVM_DIR              = S_IMDB_DIR + "svm/";
  

  
//  public static final String S_LOG_DIR
    public static final String S_MEN_FILE                 = S_PROJECT_DIR + "men/MEN_dataset_lemma.txt";
//  public static final String S_MEN_FILE                 = S_PROJECT_DIR + "men/MEN_dataset_lemma_form_full";
  
    public static final String S_SICK_FILE                = S_PROJECT_DIR + "sick/postprocessed/SICK_train_trial.txt";
    public static final String S_DATASET_DIR              = "/home/thenghiapham/work/dataset/lapata/";
    public static final String S_AN_FILE                  = S_DATASET_DIR + "an_lemma.txt";
    public static final String S_NN_FILE                  = S_DATASET_DIR + "nn_lemma.txt";
    public static final String S_VO_FILE                  = S_DATASET_DIR + "vo_lemma.txt";
    public static final String S_SV_FILE                  = S_DATASET_DIR + "sv_lemma.txt";
    
    public static final String S_VALIDATION_FILE          = S_PROJECT_DIR + "tmp_parsed/test_bnc.txt";
    
    
    
    
//    public static final String S_PROJECT_DIR              = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/";
//    public static final String S_CONSTRUCTION_FILE        = S_PROJECT_DIR + "constructions/selected-constructions1.txt";
////    public static final String S_TRAIN_DIR                = S_PROJECT_DIR + "corpus/split/bnc";
//    public static final String S_TRAIN_DIR                = S_PROJECT_DIR + "corpus/split/wiki";
////    public static final String S_TRAIN_FILE               = S_PROJECT_DIR + "corpus/bnc.txt";
//    public static final String S_TRAIN_FILE               = S_PROJECT_DIR + "corpus/wiki.parse";
//    public static final String S_OUT_DIR                  = S_PROJECT_DIR + "output/";
//    public static final String S_LOG_DIR                  = S_PROJECT_DIR  + "log/";
////    public static final String S_VECTOR_FILE              = S_OUT_DIR + "bnc.bin";
////    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "bnc.cmp";
////    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "bnc.voc";
////    public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "bnc.ini";
//    
////    public static final String S_VECTOR_FILE              = S_OUT_DIR + "s_hs_rdubnc40ts.bin";
////    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "s_hs_rdubnc40ts.cmp";
////    public static final String S_MODEL_FILE               = S_OUT_DIR + "s_hs_rdubnc40ts.mdl";
////    public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "s_hs_rdubnc40ts.ini";
////    public static final String S_LOG_FILE                 = S_LOG_DIR + "s_hs_rdubnc40ts.log";
////    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "s_hs_rdubnc40ts.voc";
//    public static final String S_VECTOR_FILE              = S_OUT_DIR + "s_neg_wlwiki40is_wo_lex.bin";
//    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "s_neg_wlwiki40is_wo_lex.cmp";
//    public static final String S_MODEL_FILE               = S_OUT_DIR + "s_neg_wlwiki40is_wo_lex.mdl";
//    public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "s_neg_wlwiki40is_wo_lex.ini";
//    public static final String S_LOG_FILE                 = S_LOG_DIR + "s_neg_wlwiki40is_wo_lex.log";
//    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "wiki_lower.voc";
//    
//    public static final String S_WORD_VECTOR_FILE         = S_OUT_DIR + "hs_skip_bnc_size.bin";
//    public static final String S_WORD_MODEL_FILE          = S_OUT_DIR + "hs_skip_bnc_size.mdl";
//    public static final String S_WORD_LOG_FILE            = S_LOG_DIR + "hs_skip_bnc_size.log";
//    
////    public static final String S_VECTOR_FILE              = S_OUT_DIR + "skip_wiki300_ss.bin";
////    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "skip_wiki300_ss.cmp";
////    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "skip_wiki300_ss.voc";
////    public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "skip_wiki300_ss.ini";
//    
//    
////    public static final String S_LOG_FILE                 = S_LOG_DIR + "wiki.log";
//    
////    public static final int S_MIN_FREQUENCY               = 5;
//    public static final int S_MIN_FREQUENCY               = 100;
//    
//  
//    public static final String S_MEN_FILE                 = S_PROJECT_DIR + "men/MEN_dataset_lemma.txt";
//    public static final String S_AN_FILE                  = S_PROJECT_DIR + "lapata/an_lemma.txt";
//    public static final String S_NN_FILE                  = S_PROJECT_DIR + "lapata/nn_lemma.txt";
//    public static final String S_VO_FILE                  = S_PROJECT_DIR + "lapata/vo_lemma.txt";
//    public static final String S_SV_FILE                  = S_PROJECT_DIR + "lapata/sv_lemma.txt";
//    public static final String S_SICK_FILE                = S_PROJECT_DIR + "sick/parsed/SICK_train_trial.txt";
//    
//    public static final String S_RTE_DIR                  = S_PROJECT_DIR + "rte/";
//    public static final String S_RTE_FILE                 = S_RTE_DIR + "SICK_train_trial.txt";
//    public static final String S_RTE_FEATURE_FILE         = S_RTE_DIR + "feature/SICK_train_trial.txt";
//    public static final String S_RTE_SVM_DIR              = S_PROJECT_DIR + "rte/svm/";
//      
//    public static final String S_IMDB_DIR                   = S_PROJECT_DIR + "imdb/";
//    public static final String S_IMDB_FILE                 = S_IMDB_DIR + "normalized_text.parsed";
//    public static final String S_IMDB_LABEL_FILE           = S_IMDB_DIR + "sentence_sentiment.txt2";
//    public static final String S_IMDB_SVM_DIR              = S_IMDB_DIR + "svm/";
    
}


