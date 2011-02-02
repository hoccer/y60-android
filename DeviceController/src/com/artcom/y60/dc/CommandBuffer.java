package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Runtime;

import com.artcom.y60.Logger;

public class CommandBuffer {

    private static final String     LOG_TAG                 = "CommandBuffer";
    private static final int        STDOUT_BUFFER_SIZE      = 1024;
    private static final int        DEFAULT_BUFFER_SIZE     = 512000;
    private static final int        CAPTURE_SLEEP_TIME      = 500; //milliseconds
    private static final int        COMMAND_WAIT_FOR_TIMEOUT= 5000;  //milliseconds
    
    private boolean                 doProcessStdoutCapture;
    private BufferedReader          processStdoutStream;
    private BufferedReader          processStderrStream;
    private Process                 commandProcess;

    private StringBuffer            commandStdoutBuffer;
    private int                     maxStdoutBufferLength;

    public CommandBuffer(int bufferSize) {
        commandStdoutBuffer = null;
        maxStdoutBufferLength = bufferSize;
        commandProcess = null;
    }

    public CommandBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    private void logToY60AndBuffer(String logMessage){
        Logger.v(LOG_TAG,logMessage);
        commandStdoutBuffer.append(">>>>>>>>: " + logMessage + "\n");
    }

    private boolean startCommandProcess(String command) {
        commandStdoutBuffer = new StringBuffer();
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

    private void trimStdoutBuffer(StringBuffer stdoutBuffer) {
        if (stdoutBuffer.length() > maxStdoutBufferLength){
            int lengthDifference = stdoutBuffer.length() - maxStdoutBufferLength;
            stdoutBuffer.delete(0,lengthDifference);
            int firstNewlinePosition = stdoutBuffer.indexOf("\n");
            if ( firstNewlinePosition > 0 ) {
                stdoutBuffer.delete(0,firstNewlinePosition+1);
            }
        }
    }

    public String getCommandBuffer() {
        if (commandStdoutBuffer != null ) {
            return commandStdoutBuffer.toString();
        } else {
            return "";
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
                waitForCommandThread.join(COMMAND_WAIT_FOR_TIMEOUT);
                if (waitForCommandThread.isAlive()){
                    logToY60AndBuffer("interrupting command after " + COMMAND_WAIT_FOR_TIMEOUT + " milliseconds.");
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

    public void stopCommandCapture() {
        doProcessStdoutCapture = true;
    }

    public void startCommandCapture(String command) {
        if (startCommandProcess(command)) {
            doProcessStdoutCapture = true;
            (new Thread() {
                public void run() {
                    while(doProcessStdoutCapture) {
                        try {
                            while(processStdoutStream.ready()) {
                                commandStdoutBuffer.append(processStdoutStream.readLine()).append("\n");
                            }
                            trimStdoutBuffer(commandStdoutBuffer);
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

}

