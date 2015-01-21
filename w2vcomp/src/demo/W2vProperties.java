package demo;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class W2vProperties{
    protected Properties properties;
    public W2vProperties(String configFile) throws IOException{
        properties = new Properties();
        properties.load(new FileReader(configFile));
        
        String projectDir = properties.getProperty("ProjectDir");
        
        String constructionFileName = properties.getProperty("ConstructionFileName");
        String constructionFilePath = projectDir + "/" + constructionFileName;
        properties.setProperty("ConstructionFile", constructionFilePath);
        
        String sTrainDir = properties.getProperty("STrainDirName");
        String sTrainDirPath = projectDir + "/" + sTrainDir;
        properties.setProperty("STrainDir", sTrainDirPath);
        
        String outputDir = projectDir + "/output";
        properties.setProperty("OutputDir", outputDir);
        String logDir = projectDir + "/log";
        properties.setProperty("LogDir", logDir);
        
        String sOutputName = properties.getProperty("SOutputFileTemplate");
        String sOutputFilePath = outputDir + "/" + sOutputName;
        properties.setProperty("SOutputFile", sOutputFilePath);
        String sLogFilePath = logDir + "/" + sOutputName;
        properties.setProperty("SLogFile", sLogFilePath);
        
        String vocabFileName = properties.getProperty("VocabFileName");
        String vocabFile = outputDir + "/" + vocabFileName;
        properties.setProperty("VocabFile", vocabFile);
        
        String wordVectorFileName = properties.getProperty("WordVectorFileName");
        String wordVectorFilePath = outputDir + "/" + wordVectorFileName;
        properties.setProperty("WordVectorFile", wordVectorFilePath);
        String wordLogFilePath = logDir + "/" + wordVectorFileName;
        properties.setProperty("WordLogFile", wordLogFilePath);
        
    }
    
}
