package io.word;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessWordStream implements WordInputStream {
    public static final int    BUFFER_SIZE      = 1024;
    protected RandomAccessFile raFile;
    protected long             beginPosition;
    protected long             endPosition;
    protected long             currentPosition;
    protected long             fileSize;

    protected byte[]           buffer;
    protected int              curBufferPos;
    protected int              endBufferPos;

    protected int              maxWordLength;
    boolean                    reachedEndOfFile = false;

    public RandomAccessWordStream(String filePath, int partNum, int index,
            int maxWordLength) throws IOException {
        this.maxWordLength = maxWordLength;

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File " + filePath + " does not exist");
        }
        fileSize = file.length();
        if (index < 0 || partNum <= 0 || index >= partNum) {
            throw new IOException("Out of file range");
        }
        long partSize = 0;
        long begin = 0;
        long end = 0;
        if (fileSize % partNum == 0) {
            partSize = fileSize / partNum;
        } else {
            partSize = fileSize / partNum + 1;
        }
        if (index < partNum - 1) {
            begin = partSize * index;
            end = partSize * (index + 1);
        } else {
            begin = partSize * index;
            end = fileSize;
        }
        System.out.println("thread: " + index + ", begin: " + begin + ", end: "
                + end);
        init(filePath, begin, end);
    }

    public RandomAccessWordStream(String filePath, long begin, long end,
            int maxWordLength) throws IOException {
        this.maxWordLength = maxWordLength;

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File " + filePath + " does not exist");
        }
        fileSize = file.length();
        if (begin < 0 || end <= 0 || begin >= fileSize || end > fileSize
                || begin >= end) {
            throw new IOException("Out of file range");
        }
        init(filePath, begin, end);
    }

    protected void init(String filePath, long begin, long end)
            throws IOException {

        beginPosition = begin;
        endPosition = end;
        currentPosition = beginPosition;

        raFile = new RandomAccessFile(filePath, "r");
        raFile.seek(beginPosition);
        buffer = new byte[BUFFER_SIZE];
        loadBuffer();
    }

    protected void loadBuffer() {
        try {
            // System.out.println("current Pos: " + currentPosition);
            if (currentPosition >= endPosition) {
                // System.out.println(" end of :" + endPosition);
                curBufferPos = 0;
                endBufferPos = 0;
                return;
            }
            int numByte = raFile.read(buffer);
            if (numByte == -1) {
                currentPosition = endPosition;
                curBufferPos = 0;
                endBufferPos = 0;
            } else {
                if (currentPosition + BUFFER_SIZE < endPosition) {
                    currentPosition += BUFFER_SIZE;
                    curBufferPos = 0;
                    endBufferPos = BUFFER_SIZE;
                } else {
                    curBufferPos = 0;
                    endBufferPos = (int) (endPosition - currentPosition);
                    currentPosition = endPosition;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void pushBack() {
        curBufferPos--;
    }

    protected int nextByte() {
        // check end the end of buffer
        if (curBufferPos >= endBufferPos) {
            if (endBufferPos < BUFFER_SIZE) {
                return -1;
            } else {
                loadBuffer();
            }
        }

        // read next byte from buffer
        if (curBufferPos < endBufferPos) {
            curBufferPos++;
            return buffer[curBufferPos - 1];
        } else {
            return -1;
        }
    }

    @Override
    public String readWord() throws IOException {
        StringBuffer buff = new StringBuffer();
        boolean newString = true;
        char ch;
        int nextCh;
        while (true) {
            nextCh = nextByte();
            if (nextCh == -1) {
                reachedEndOfFile = true;
                break;
            }
            ch = (char) nextCh;
            // for window character
            if (ch == 13)
                continue;
            if ((ch == ' ') || (ch == '\t') || (ch == '\n')) {
                if (!newString) {
                    if (ch == '\n') {
                        pushBack();
                    }
                    break;
                }
                // end of line = end of sentence
                if (ch == '\n') {
                    return "</s>";
                } else {
                    continue;
                }
            }
            buff.append(ch);
            newString = false;
        }

        // the end of the file, discard the last word, just in case
        if (nextCh == -1) {
            return "";
        }

        String result = buff.toString();
        if (result.length() > maxWordLength) {
            return result.substring(0, maxWordLength);
        } else {
            return result;
        }
    }

    @Override
    public void close() throws IOException {
        raFile.close();
    }

    @Override
    public boolean endOfFile() {
        return reachedEndOfFile;
    }

}
