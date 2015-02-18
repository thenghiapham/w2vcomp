package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ParsedDataShuffler {
    public static HashMap<String, Integer> list2Dict(File[] files) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        System.out.println("result is null" + (result == null));
        for (int i = 0; i < files.length; i++) {
            System.out.println("files["+ i + "] is null" + (files[i] == null));
            result.put(files[i].getAbsolutePath(), i);
        }
        return result;
    }
    
    public static File[] getFileList(String[] dirPaths) {
        int count = 0;
        File[][] fileLists = new File[dirPaths.length][];
        for (int i = 0; i < dirPaths.length; i++) {
            String dirPath = dirPaths[i];
            File dir = new File(dirPath);
            fileLists[i] = dir.listFiles();
            Arrays.sort(fileLists[i]);
            count += fileLists[i].length;
        }
        File[] result = new File[count];
        count = 0;
        for (int i = 0; i < dirPaths.length; i++) {
            System.arraycopy(fileLists[i], 0, result, count, fileLists[i].length);
        }
        return result;
    }
    
    public static void shuffleFile(String[] dirPaths, String outputFile) throws IOException {
        File[] files = getFileList(dirPaths);
        HashMap<String, Integer> file2Index = list2Dict(files);
        List<File> fileList = Arrays.asList(files);
        Collections.shuffle(fileList);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for (File file: fileList) {
            printFile(file, file2Index, files, writer);
            System.out.println(file.getAbsolutePath());
        }
    }
    
    public static void printFile(File file, HashMap<String, Integer> f2index, 
            File[] files, BufferedWriter writer) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String textId = seekFirstDoc(reader);
        if (textId != null) {
            // write until the end of file
            String line = textId;
            while (line != null) {
                writer.write(line + "\n");
                line = reader.readLine();
            }
            reader.close();
            
            int index = f2index.get(file.getAbsolutePath());
            boolean endDoc = false;
            int count = 0;
            while (!endDoc) {
                
                index++;
                if (index == files.length) {
                    endDoc = true;
                    break;
                } else {
                    reader = new BufferedReader(new FileReader(files[index]));
                    line = reader.readLine();
                    while (line != null) {
                        if (line.startsWith("<text id")) {
                            endDoc = true;
                            break;
                        }
                        count++;
                        writer.write(line + "\n");
                        line = reader.readLine();
                    }
                    reader.close();
                }
            }
            System.out.println("write: " + count + " lines more");
        }
    }
    
    public static String seekFirstDoc(BufferedReader reader) throws IOException{
        String line = reader.readLine();
        int count = 0;
        while (line != null) {
            if (line.startsWith("<text id")) {
                System.out.println("skipping " + count + " lines");
                return line;
            }
            count++;
            line = reader.readLine();
        }
        System.out.println("skipping " + count + " lines");
        return null;
    }
    
    
    public static void main(String[] args) throws IOException{
        // terrible bugs here in the the server
        String inputDir = args[0];
        String outputFile = args[1];
        String bncPath = inputDir + "/parsed_bnc";
        String wikiPath = inputDir + "/parsed_wiki";
        String ukwacPath = inputDir + "/parsed_ukwac";
        String[] subDirs = {bncPath, wikiPath, ukwacPath};
//        String[] subDirs = {bncPath};
        shuffleFile(subDirs, outputFile);
    }
}
