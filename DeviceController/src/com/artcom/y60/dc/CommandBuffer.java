package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Runtime;

import java.io.FileReader;
import java.io.FileWriter;

import java.util.regex.Pattern;

import com.artcom.y60.Logger;

public class CommandBuffer {

    // +------------------------+
    // +     Static Members     +
    // +------------------------+
    private static final String     LOG_TAG                             = "CommandBuffer";
    private static final int        STDOUT_BUFFER_SIZE                  = 1024;
    private static final int        DEFAULT_BUFFER_SIZE                 = 50000; //512000;
    private static final int        CAPTURE_SLEEP_TIME                  = 1000;  //milliseconds
    private static final int        WAIT_FOR_COMMAND_TIMEOUT            = 5000; //milliseconds
    private static final String     PERSISTENT_BUFFER_DEFAULT_FILENAME  = "/sdcard/logcat.txt";
    private static final int        BUFFER_SIZE_FOR_TIMESTAMP_SEARCH    = 5000;
    private static final int        BUFFER_FILE_VISIBLE_LENGTH          = 10000;
    private static final String     TIMESTAMP_REGULAR_PATTERN           = "\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}";
    private static final int        TIMESTAMP_STRING_LENGTH             = 19;

    
    // +-------------------------+
    // +     Private Members     +
    // +-------------------------+
    private boolean                 doProcessStdoutCapture;
    private BufferedReader          processStdoutStream;
    private BufferedReader          processStderrStream;
    private Process                 commandProcess;
    private String                  persistentBufferFileName;

    private StringBuffer            commandStdoutBuffer;
    private int                     maxStdoutBufferLength;
    private String                  lastTimeStampFromBufferFile;

    // +----------------------+
    // +     Constructors     +
    // +----------------------+
    public CommandBuffer(int bufferSize,String bufferFileName) {
        commandStdoutBuffer = null;
        maxStdoutBufferLength = bufferSize;
        commandProcess = null;
        lastTimeStampFromBufferFile = null;
        persistentBufferFileName = bufferFileName;
    }

    public CommandBuffer() {
        this(DEFAULT_BUFFER_SIZE, PERSISTENT_BUFFER_DEFAULT_FILENAME);
    }
    public CommandBuffer(int bufferSize) {
        this(bufferSize, PERSISTENT_BUFFER_DEFAULT_FILENAME);
    }
    public CommandBuffer(String bufferFileName) {
        this(DEFAULT_BUFFER_SIZE, bufferFileName);
    }

    // +-------------------------+
    // +     Private Methods     +
    // +-------------------------+

    //      +------------------------+
    //      +     Static Helpers     +
    //      +------------------------+
    public static void writeStringToFile(String text, String fileName) {
        try{
            FileWriter fw;
            fw = new FileWriter(fileName,true);
            fw.write(text);
            fw.close();
        } catch( IOException e ){
            Logger.v(LOG_TAG,"IOException during writing buffer to: ", fileName);
        }
    }

    public static void clearBuffer(StringBuffer buffer) {
        buffer.delete(0,buffer.length());
    }

    public static String getFromEndOfBufferedReader(int bytesToRead, BufferedReader buffer,long bufferSize){
        String  readString = "";
        int     bytesToSkip = (int)(bufferSize - bytesToRead);
        try {
            if (bytesToSkip > 0) {
                char            charArray[] = new char[bytesToRead];
                buffer.skip(bytesToSkip);
                buffer.read(charArray, 0, bytesToRead);
                readString = new String(charArray);
                int firstNewlinePosition = readString.indexOf("\n");
                if ( (firstNewlinePosition > 0) && (readString.length() > (firstNewlinePosition+1)) ) {
                    readString = readString.substring(firstNewlinePosition + 1);
                }
            } else {
                char            charArray[] = new char[(int)bufferSize];
                buffer.read(charArray, 0, (int)bufferSize);
                readString = new String(charArray);
            }
        } catch (IOException e){
            Logger.v(LOG_TAG,"IOException during reading from Buffer: ", e);
        }
        return readString;
    }

    public static String getFromEndOfBufferFile(int bytesToRead, String fileName){
        String  fileText = "";
        File    bufferFile = new File(fileName);
        if (bufferFile.exists()){
            try {
                long            fileSize = bufferFile.length();
                BufferedReader  bufferedFileReader = new BufferedReader(new FileReader(bufferFile));
                fileText = getFromEndOfBufferedReader(bytesToRead,bufferedFileReader,fileSize);
                bufferedFileReader.close();
            } catch (FileNotFoundException e){
                Logger.v(LOG_TAG,"could not open File Reader for file: " + fileName);
            } catch (IOException e){
                Logger.v(LOG_TAG,"could not read from BufferedReader from file: " + fileName);
            }
        }
        return fileText;
    }

    public static String getLastTimeStampFromString(String buffer){
        String foundTimeStamp = null;
        int lastNewLinePosition = buffer.lastIndexOf("\n");
        if (lastNewLinePosition > 0){
            int secondLastNewLinePosition = buffer.lastIndexOf("\n",lastNewLinePosition-1);
            if (secondLastNewLinePosition > 0){
                int timeStampEndOffset = secondLastNewLinePosition + TIMESTAMP_STRING_LENGTH; 
                if (timeStampEndOffset < buffer.length() ) {
                    foundTimeStamp = buffer.substring(secondLastNewLinePosition+1,
                        timeStampEndOffset);
                    if (!Pattern.matches(TIMESTAMP_REGULAR_PATTERN,foundTimeStamp)) {
                        foundTimeStamp = null;
                    }
                }
            }
        }
        return foundTimeStamp;
    }

    public static boolean clearBufferUntilTimeStamp(StringBuffer buffer, String timeStamp){
        boolean foundTimeStamp = false;
        if (timeStamp != null ){
            int timeStampPosition = buffer.indexOf(timeStamp);
            if(timeStampPosition > 0) {
                int firstNewLineAfterTimeStampPosition = buffer.indexOf("\n",timeStampPosition);
                if (firstNewLineAfterTimeStampPosition > 0 ){
                    buffer.delete(0,firstNewLineAfterTimeStampPosition+1);
                }
                foundTimeStamp = true;
            }
        }
        return foundTimeStamp;
    }

    public static void flushBufferToFile(StringBuffer buffer, String fileName) {
        writeStringToFile(buffer.toString(),fileName);
        clearBuffer(buffer);
    }

    //      +------------------------+

    private void logToY60AndBuffer(String logMessage){
        Logger.v(LOG_TAG,logMessage);
        commandStdoutBuffer.append(">>>>>>>>: " + logMessage + "\n");
    }

    private void logToBuffer(String logMessage){
        commandStdoutBuffer.append(">>>>>>>>: " + logMessage + "\n");
    }

    private boolean startCommandProcess(String command) {
        commandStdoutBuffer = new StringBuffer();
        lastTimeStampFromBufferFile = null;
        try {
            commandProcess = Runtime.getRuntime().exec(command);
        } catch(IOException e){
            logToY60AndBuffer("could not start command: " + command + " : " + e);
            return false;
        }
        processStdoutStream = new BufferedReader( new InputStreamReader(commandProcess.getInputStream()),
                STDOUT_BUFFER_SIZE );
        processStderrStream = new BufferedReader( new InputStreamReader(commandProcess.getErrorStream()),
                STDOUT_BUFFER_SIZE );
        return true;
    }
    
    private void stopCommandProcess() {
        commandProcess.destroy();
        commandProcess = null;
        Logger.v(LOG_TAG,"destoyed command process");

        try {
            processStdoutStream.close();
            Logger.v(LOG_TAG,"closed Stdout BufferedReader");
        } catch (IOException e){
            Logger.v(LOG_TAG,"could not close Stdout BufferedReader: ", e);
        }

        try {
            processStderrStream.close();
            Logger.v(LOG_TAG,"closed Stderr BufferedReader");
        } catch (IOException e){
            Logger.v(LOG_TAG,"could not close Stderr BufferedReader: ", e);
        }
    }

    // +------------------------+
    // +     Public Methods     +
    // +------------------------+

    public String getCommandBuffer() {
        if (commandStdoutBuffer != null ) {
            return commandStdoutBuffer.toString();
        } else {
            return "";
        }          
    }

    public String getCommandBufferFromFile() {
        flushBufferToFile(commandStdoutBuffer,persistentBufferFileName);
        return getFromEndOfBufferFile(BUFFER_FILE_VISIBLE_LENGTH,persistentBufferFileName);
    }

    public void stopCommandCapture() {
        doProcessStdoutCapture = true;
    }

    public void startCommandCapture(String command) {
        if (startCommandProcess(command)) {
            doProcessStdoutCapture = true;
            
            String fileBuffer = getFromEndOfBufferFile(BUFFER_SIZE_FOR_TIMESTAMP_SEARCH, persistentBufferFileName);
            lastTimeStampFromBufferFile = getLastTimeStampFromString(fileBuffer);

            (new Thread() {
                public void run() {
                    while(doProcessStdoutCapture) {
                        try {
                            while(processStdoutStream.ready()) {
                                commandStdoutBuffer.append(processStdoutStream.readLine()).append("\n");
                            }
                            if (commandStdoutBuffer.length() > maxStdoutBufferLength){
                                if (clearBufferUntilTimeStamp(commandStdoutBuffer,lastTimeStampFromBufferFile)) {
                                    lastTimeStampFromBufferFile = null;
                                }
                                flushBufferToFile(commandStdoutBuffer,persistentBufferFileName);
                            }
                        } catch(IOException e) {
                            Logger.v(LOG_TAG, "IOException during buffer reading: " , e);
                            break;
                        }
                        try {
                            Thread.sleep(CAPTURE_SLEEP_TIME);
                        } catch(InterruptedException e){
                            Logger.v(LOG_TAG, "InterruptedException during sleep" , e);
                            break;
                        }
                    }
                    stopCommandProcess();
                }
            }).start();
        }
    }

    public void getCommandOutput(String command) {
        if (startCommandProcess(command)) {
            logToY60AndBuffer("executing command: " + command);
            Thread waitForCommandThread = new Thread() {
                public void run() {
                    try {
                        commandProcess.waitFor();
                    } catch (InterruptedException e){
                        logToY60AndBuffer("InterruptedException during waiting for command to finish");
                    }
                }
            };
            waitForCommandThread.start();
            try {
                waitForCommandThread.join(WAIT_FOR_COMMAND_TIMEOUT);
                if (waitForCommandThread.isAlive()){
                    logToY60AndBuffer("interrupting command after " + WAIT_FOR_COMMAND_TIMEOUT + " milliseconds.");
                }
                waitForCommandThread.interrupt();
                waitForCommandThread.join();
            } catch(InterruptedException e){
                logToY60AndBuffer("InterruptedException during finishing command");
            }

            try {
                commandStdoutBuffer.append("STDERR:\n");
                while(processStderrStream.ready()) {
                    commandStdoutBuffer.append(processStderrStream.readLine()).append("\n");
                }
                commandStdoutBuffer.append("STDOUT:\n");
                while(processStdoutStream.ready()) {
                    commandStdoutBuffer.append(processStdoutStream.readLine()).append("\n");
                }
            } catch(IOException e){
                logToY60AndBuffer("IOException during reading from stdout/stderr buffer.");
            }
            stopCommandProcess();
        } else {
            logToY60AndBuffer("command: " + command + " could not be started.");
        }
    }

}

