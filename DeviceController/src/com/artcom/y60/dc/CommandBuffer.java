package com.artcom.y60.dc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Runtime;


import java.io.FileReader;
import java.io.FileWriter;

import java.util.regex.Pattern;

import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

public class CommandBuffer {

    // +------------------------+
    // +     Static Members     +
    // +------------------------+
    private static final String     LOG_TAG                             = "CommandBuffer";
    private static final int        STDOUT_BUFFER_SIZE                  = 1024;
    private static final int        DEFAULT_BUFFER_SIZE                 = 50000;
    private static final int        CAPTURE_SLEEP_TIME                  = 1000;  //milliseconds
    private static final int        WAIT_FOR_COMMAND_TIMEOUT            = 5000;  //milliseconds
    private static final String     PERSISTENT_BUFFER_DEFAULT_FILENAME  = "/sdcard/logcat.txt";
    private static final int        BUFFER_SIZE_FOR_TIMESTAMP_SEARCH    = 5000;
    private static final long       BUFFER_FILE_VISIBLE_LENGTH          = 50000;
    private static final long       BUFFER_FILE_MAXIMAL_LENGTH          = 50000000;
    private static final String     TIMESTAMP_SEARCH_PATTERN            = "\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}";
    private static final int        TIMESTAMP_STRING_LENGTH             = 19;
    private static final int        WAIT_FOR_NO_CAPTURE_BLOCKING        = 3000; //milliseconds
    private static final int        WAIT_FOR_BUFFER_READY               = 500;  // milliseconds

    
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

    private String                  exceptionMessage;

    private boolean                 blockFileAccessByCapture;
    private boolean                 blockFileAccessByRequest;

    private BroadcastReceiver       writeBufferToFileReceiver;
    private BroadcastReceiver       deleteBufferFileReceiver;
    private Context                 systemContext;

    // +----------------------+
    // +     Constructors     +
    // +----------------------+
    public CommandBuffer(int bufferSize,String bufferFileName, Context context) {
        commandStdoutBuffer = null;
        maxStdoutBufferLength = bufferSize;
        commandProcess = null;
        lastTimeStampFromBufferFile = null;
        persistentBufferFileName = bufferFileName;
        exceptionMessage = null;
        blockFileAccessByCapture = false;
        blockFileAccessByRequest = false;
        writeBufferToFileReceiver = null;
        deleteBufferFileReceiver = null;
        systemContext = context;
    }

    public CommandBuffer(Context context) {
        this(DEFAULT_BUFFER_SIZE, PERSISTENT_BUFFER_DEFAULT_FILENAME, context);
    }
    public CommandBuffer(int bufferSize, Context context) {
        this(bufferSize, PERSISTENT_BUFFER_DEFAULT_FILENAME, context);
    }
    public CommandBuffer(String bufferFileName, Context context) {
        this(DEFAULT_BUFFER_SIZE, bufferFileName, context);
    }

    // +------------------------+
    // +     Static Helpers     +
    // +------------------------+
    public static void writeStringToFile(String text, String fileName) throws IOException {
        FileWriter fw;
        fw = new FileWriter(fileName,true);
        fw.write(text);
        fw.close();
    }

    public static void clearBuffer(StringBuffer buffer) {
        buffer.delete(0,buffer.length());
    }

    public static String getFromEndOfBufferedReader(long bytesToRead, BufferedReader buffer, long bufferSize)
        throws IOException {
        String  readString = "";
        long     bytesToSkip = bufferSize - bytesToRead;
        if (bytesToSkip > 0) {
            for(int i=0;i<(int)(WAIT_FOR_BUFFER_READY/50);++i){
                if (buffer.ready() ) {
                    break;
                }
                try{
                    Thread.sleep(50);
                } catch(InterruptedException e){
                    Logger.v(LOG_TAG,"InterruptedException during waiting for file bufferedReader to be ready ",e);
                }
            }
            if (buffer.ready() ) {
                char    charArray[] = new char[(int)bytesToRead];
                long    actuallySkipped = buffer.skip(bytesToSkip);
                int     actullyReadBytes = buffer.read(charArray, 0, (int)bytesToRead);
                readString = new String(charArray,0,actullyReadBytes);
                int firstNewlinePosition = readString.indexOf("\n");
                if ( (firstNewlinePosition > 0) && (readString.length() > (firstNewlinePosition+1)) ) {
                    readString = readString.substring(firstNewlinePosition + 1);
                }
            } else {
                Logger.v(LOG_TAG,"buffer is NOT ready");
            }
        } else {
            char    charArray[] = new char[(int)bufferSize];
            int     actullyReadBytes = buffer.read(charArray, 0, (int)bufferSize);
            readString = new String(charArray,0,actullyReadBytes);
        }
        return readString;
    }

    public static String getFromEndOfBufferFile(long bytesToRead, String fileName)
        throws IOException, FileNotFoundException{
        String  fileText = "";
        File bufferFile = new File(fileName);
        if (bufferFile.exists()){
            BufferedReader  bufferedFileReader = new BufferedReader(new FileReader(fileName));
            fileText = getFromEndOfBufferedReader(bytesToRead,bufferedFileReader,getFileByteSize(fileName));
            bufferedFileReader.close();
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
                    if (!Pattern.matches(TIMESTAMP_SEARCH_PATTERN,foundTimeStamp)) {
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

    public static void flushBufferToFile(StringBuffer buffer, String fileName) throws IOException {
        writeStringToFile(buffer.toString(),fileName);
        clearBuffer(buffer);
    }

    private static void deleteFile(String fileName) {
        File    bufferFile = new File(fileName);
        if (bufferFile.exists()){
            bufferFile.delete();
        }
    }

    private static long getFileByteSize(String fileName) {
        long    fileSize = 0;
        File    bufferFile = new File(fileName);
        if (bufferFile.exists()){
            fileSize = bufferFile.length();
        }
        return fileSize;
    }

    private static void checkFileSize(String fileName) {
        if (getFileByteSize(fileName) > BUFFER_FILE_MAXIMAL_LENGTH) {
            Logger.v(LOG_TAG,"deleting buffer file because its size exceeds the maximal size of: ",
                BUFFER_FILE_MAXIMAL_LENGTH);
            deleteFile(fileName);
        }
    }

    // +-------------------------+
    // +     Private Methods     +
    // +-------------------------+

    private String logException(String message, Throwable exception){
        exceptionMessage = message + exception;
        StackTraceElement[] stackElements;
        Throwable throwable = exception;
        while(throwable != null){
            stackElements = throwable.getStackTrace();
            for(int i=0;i<stackElements.length;++i){
                exceptionMessage += stackElements[i].toString() + "\n";
            }
            throwable = throwable.getCause();
        }
        return exceptionMessage;
    }

    private void logToY60AndBuffer(String logMessage){
        Logger.v(LOG_TAG,logMessage);
        commandStdoutBuffer.append(">>>>>>>>: " + logMessage + "\n");
    }

    private boolean startCommandProcess(String command) {
        commandStdoutBuffer = new StringBuffer();
        lastTimeStampFromBufferFile = null;
        exceptionMessage = null;
        try {
            commandProcess = Runtime.getRuntime().exec(command);
        } catch(IllegalArgumentException e){
            logToY60AndBuffer(logException("could not start command: " + command + "\n", e));
            return false;
        } catch(IOException e){
            logToY60AndBuffer(logException("could not start command: " + command + "\n", e));
            return false;
        }
        try {
            processStdoutStream = new BufferedReader( new InputStreamReader(commandProcess.getInputStream(),"ISO-8859-1"),
                    STDOUT_BUFFER_SIZE );
            processStderrStream = new BufferedReader( new InputStreamReader(commandProcess.getErrorStream(),"ISO-8859-1"),
                    STDOUT_BUFFER_SIZE );
        } catch(UnsupportedEncodingException e){
            Logger.v(LOG_TAG,"UnsupportedEncodingException at InputStreamReader creation ",e);
        }

        registerBroadcastReceiver();

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

        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        writeBufferToFileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                Logger.v(LOG_TAG,"got flush buffer to file on sdcard BC request");
                try {
                    if( (commandStdoutBuffer!=null)&&(persistentBufferFileName!=null) ){
                        flushBufferToFile(commandStdoutBuffer,persistentBufferFileName);
                    }
                } catch (IOException e){
                    Logger.v(LOG_TAG, "could not write buffer to sdcard on BC request");
                }
            }
        };
        systemContext.registerReceiver(writeBufferToFileReceiver, new IntentFilter(Y60Action.COMMAND_BUFFER_FLUSH_BC));

        deleteBufferFileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                deleteFile(persistentBufferFileName);
                Logger.v(LOG_TAG,"got delete buffer file on sdcard BC request");
            }
        };
        systemContext.registerReceiver(deleteBufferFileReceiver, new IntentFilter(Y60Action.COMMAND_BUFFER_DELETE_BC));
    }

    private void unregisterBroadcastReceiver() {
        if (writeBufferToFileReceiver != null) {
            systemContext.unregisterReceiver(writeBufferToFileReceiver);
            writeBufferToFileReceiver = null;
        }
        if (deleteBufferFileReceiver != null) {
            systemContext.unregisterReceiver(deleteBufferFileReceiver);
            deleteBufferFileReceiver = null;
        }
    }

    // +------------------------+
    // +     Public Methods     +
    // +------------------------+

    public String getCommandBufferFromRam() {
        if (commandStdoutBuffer != null ) {
            return commandStdoutBuffer.toString();
        } else {
            return null;
        }          
    }

    public String getCommandBufferFromFile() {
        return getCommandBufferFromFile(BUFFER_FILE_VISIBLE_LENGTH);
    }

    public String getCommandBufferFromFile(long visibleCharacters) {
        blockFileAccessByRequest = true;
        if (blockFileAccessByCapture){
            for (int i=0;i<(WAIT_FOR_NO_CAPTURE_BLOCKING/100);++i){
                if (!blockFileAccessByCapture) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e){
                    Logger.v(LOG_TAG,logException("Thread.sleep Error: ", e));
                    blockFileAccessByRequest = false;
                    return null;
                }
            }
            if (blockFileAccessByCapture){
                blockFileAccessByRequest = false;
                return "could not read from command capture file because file access was blocked for too long time by the main capture process";
            }
        }
        try {
            flushBufferToFile(commandStdoutBuffer,persistentBufferFileName);
            String logText = getFromEndOfBufferFile(visibleCharacters,persistentBufferFileName);
            blockFileAccessByRequest = false;
            return logText;
        } catch (FileNotFoundException e){
            Logger.v(LOG_TAG,logException("File open Error: ", e));
        } catch (IOException e){
            Logger.v(LOG_TAG,logException("BufferedReader Error: ", e));
        }
        blockFileAccessByRequest = false;
        return null;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void stopCommandAndCapture() {
        doProcessStdoutCapture = false;
    }

    public void executeNonReturningCommandAndCapture(String command) {
        if (startCommandProcess(command)) {
            doProcessStdoutCapture = true;
            blockFileAccessByCapture = false;
            String fileBuffer = null;
            try{
                fileBuffer = getFromEndOfBufferFile(BUFFER_SIZE_FOR_TIMESTAMP_SEARCH, persistentBufferFileName);
            } catch (FileNotFoundException e){
                Logger.v(LOG_TAG,logException("File open Error: ", e));
            } catch (IOException e){
                Logger.v(LOG_TAG,logException("BufferedReader Error: ", e));
            }
            lastTimeStampFromBufferFile = getLastTimeStampFromString(fileBuffer);

            (new Thread() {
                public void run() {
                    while(doProcessStdoutCapture) {
                        try {
                            Thread.sleep(CAPTURE_SLEEP_TIME);
                        } catch(InterruptedException e){
                            Logger.v(LOG_TAG,logException("Thread.sleep Error: ", e));
                            break;
                        }
                        if (exceptionMessage != null){
                            break;
                        }
                        try {
                            while(processStdoutStream.ready()) {
                                commandStdoutBuffer.append(processStdoutStream.readLine()).append("\n");
                            }
                            if (commandStdoutBuffer.length() > maxStdoutBufferLength){
                                if (clearBufferUntilTimeStamp(commandStdoutBuffer,lastTimeStampFromBufferFile)) {
                                    lastTimeStampFromBufferFile = null;
                                }
                                if (!blockFileAccessByRequest){
                                    blockFileAccessByCapture = true;
                                    try {
                                        Logger.v(LOG_TAG,"writing command buffer to sdcard file: ", persistentBufferFileName);
                                        checkFileSize(persistentBufferFileName);
                                        flushBufferToFile(commandStdoutBuffer,persistentBufferFileName);
                                    } catch( IOException e ){
                                        Logger.v(LOG_TAG,logException("BufferedReader Error: ", e));
                                    }
                                    blockFileAccessByCapture = false;
                                }
                            }
                        } catch(IOException e) {
                            Logger.v(LOG_TAG,logException("BufferedReader Error: ", e));
                            break;
                        }
                    }
                    stopCommandProcess();
                    Logger.v(LOG_TAG,"STOPPING command output capturing");
                }
            }).start();
        }
    }

    public void executeReturningCommand(String command) {
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
                logToY60AndBuffer(logException("InterruptedException during finishing command" + command, e));
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
                logToY60AndBuffer(logException("IOException during reading from stdout/stderr buffer.", e));
            }
            stopCommandProcess();
        }
    }

}

