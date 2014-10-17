package demo;

public class TestConstants {


    
    public static final String TRAIN_FILE               = "/home/thenghiapham/svn/word2vec/text8";
    public static final String VECTOR_FILE              = "/home/thenghiapham/svn/word2vec/out.bin";
    public static final String VOCABULARY_FILE          = "/home/thenghiapham/svn/word2vec/out.voc";
    public static final String INITIALIZATION_FILE      = "/home/thenghiapham/svn/word2vec/out.ini";
    
    public static final String S_PROJECT_DIR              = "/home/thenghiapham/work/project/mikolov/";
//    public static final String S_PROJECT_DIR              = "/mnt/cimec-storage-sata/users/thenghia.pham/data/project/mikcom/";
    public static final String S_CONSTRUCTION_FILE        = S_PROJECT_DIR + "selected-constructions.txt";
    public static final String S_TRAIN_FILE               = S_PROJECT_DIR + "tmp_parsed/bnc.txt";
//    public static final String S_TRAIN_FILE               = S_PROJECT_DIR + "parse/bnc/bnc.txt";
//    public static final String S_OUT_DIR                  = S_PROJECT_DIR + "output/identity/";
    public static final String S_OUT_DIR                  = S_PROJECT_DIR + "output/";
//    public static final String S_OUT_DIR                  = S_PROJECT_DIR + "output/0.7/";
//    public static final String S_OUT_DIR                  = S_PROJECT_DIR + "output/tanh/";
    public static final String S_VECTOR_FILE              = S_OUT_DIR + "dbnc.bin";
    public static final String S_COMPOSITION_FILE         = S_OUT_DIR + "dbnc.cmp";
    public static final String S_VOCABULARY_FILE          = S_OUT_DIR + "bnc.voc";
    public static final String S_INITIALIZATION_FILE      = S_OUT_DIR + "bnc.ini";
    public static final String S_LOG_DIR                  = S_PROJECT_DIR + "log/";
    public static final String S_LOG_FILE                 = S_LOG_DIR + "sentence.log";
    
//    public static final String S_LOG_DIR
    public static final String S_MEN_FILE                 = S_PROJECT_DIR + "men/MEN_dataset_lemma.txt";
    
    


    // on the cluster
    public static final String PATH                     = "/mnt/cimec-storage-sata/users/marco.baroni/share/ukwac-maltparsing/data/";
    public static final String GZIP_TRAIN_FILES         = PATH
                                                                + "bnc.xml.gz;"
                                                                + PATH
                                                                + "ukwac1.xml.gz;"
                                                                + PATH
                                                                + "ukwac2.xml.gz;"
                                                                + PATH
                                                                + "ukwac3.xml.gz;"
                                                                + PATH
                                                                + "ukwac4.xml.gz;"
                                                                + PATH
                                                                + "ukwac5.xml.gz;"
                                                                + PATH
                                                                + "wikipedia-1.xml.gz;"
                                                                + PATH
                                                                + "wikipedia-2.xml.gz;"
                                                                + PATH
                                                                + "wikipedia-3.xml.gz;"
                                                                + PATH
                                                                + "wikipedia-4.xml.gz";
    public static final String OUT_PATH                 = "/home/pham/work/project/mikolov/output/";
    public static final String GZIP_VECTOR_FILE         = OUT_PATH
                                                                + "testVector.bin";
    public static final String GZIP_VOCABULARY_FILE     = OUT_PATH
                                                                + "testVector.voc";
    public static final String GZIP_INITIALIZATION_FILE = OUT_PATH
                                                                + "testVector.ini";
    
    
    public static final String CCG_MEN_FILE             = "/home/thenghiapham/work/project/mikolov/men/subMen.txt";
    public static final String CCG_AN_FILE              = "/home/thenghiapham/work/dataset/lapata/nn_lemmapos.txt";
    public static final String CCG_TRAIN_FILE           = "/home/thenghiapham/work/project/mikolov/ccg_text/wikiA.txt";
    public static final String CCG_VECTOR_FILE          = "/home/thenghiapham/work/project/mikolov/output/phrase1.bin";
    public static final String CCG_VOCABULARY_FILE      = "/home/thenghiapham/work/project/mikolov/output/phrase1.voc";
    public static final String CCG_INITIALIZATION_FILE  = "/home/thenghiapham/work/project/mikolov/output/phrase1.ini";
    public static final String CCG_MATRIX_FILE          = "/home/thenghiapham/work/project/mikolov/output/phrase1.comp.mat";
}
