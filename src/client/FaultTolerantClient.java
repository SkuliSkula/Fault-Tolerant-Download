package client;

import org.json.simple.JSONObject;
import utility.ChecksumUtil;
import utility.CustomProtocol;
import utility.JsonConstants;
import utility.TimeUtil;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class FaultTolerantClient extends Thread {

    private int PORT = 6789;
    private String HOST = "localhost";
    private final String fileStorageLocation = "C:/Temp/Test/";

    private Socket clientSocket;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;

    private int bufferSize; // Packet size
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private CustomProtocol customProtocol;
    private boolean appendToFile;
    private boolean resume;
    private JSONObject configFile;
    private JSONObject resumeFile;
    private String requestFileName;
    private JSONObject dataFromServer;
    public FaultTolerantClient(String requestFileName, int bufferSize) {
        this.requestFileName = requestFileName;
        this.bufferSize = bufferSize;
        try{
            appendToFile = false;
            customProtocol = new CustomProtocol();
            readConfigFile();
            readResumeFile();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readResumeFile() {
        resumeFile = customProtocol.readJsonFromFile(JsonConstants.RESUME_FILE);
        resume =(double) resumeFile.get(JsonConstants.KEYRESUME) <
                (double) configFile.get(JsonConstants.KEYNUMBEROFBLOCKS) &&
                (double) configFile.get(JsonConstants.KEYNUMBEROFBLOCKS) > 0;
    }

    private void readConfigFile() {
        configFile = customProtocol.readJsonFromFile(JsonConstants.CONFIG_FILE);
    }

    public synchronized void run() {
        try {
            requestDownload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestDownload() throws IOException {

        try {
            clientSocket = new Socket(HOST,PORT);

            // Create the streams to send and receive Message objects
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            if(!resume) { // new file
                appendToFile = false;
                // Request a file
                customProtocol.fileRequest(requestFileName, bufferSize);
                // Send the request to the server
                objectOutputStream.writeObject(customProtocol.getOverhead());
                // Get message back from the server
                JSONObject fromServer = (JSONObject) objectInputStream.readObject();
                // Write to the response to config file
                customProtocol.writeJsonToFile(JsonConstants.CONFIG_FILE, fromServer);
                // Start receiving the file
                receiveFile(requestFileName);
            }
            else{ // Resume download
                // Check how many package we already received
                double savedNoPackage = getStoredAmountOfPackage();
                // Get the fileName from the config file
                String fileName = (String) configFile.get(JsonConstants.KEYFILE);
                // Create a message to the server
                customProtocol.blockRequest(fileName,bufferSize, savedNoPackage);
                // Send a request to the server
                objectOutputStream.writeObject(customProtocol.getOverhead());
                appendToFile = true;
                receiveFile(fileName);
            }

        }catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(objectInputStream != null)
                objectInputStream.close();
            if(objectOutputStream != null) {
                objectOutputStream.flush();
                objectOutputStream.close();
            }
        }
    }

    private void receiveFile(String fileName) throws IOException {
            long startTime = 0;
        try{
            fileOutputStream = new FileOutputStream(fileStorageLocation + "FT" +fileName, appendToFile);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            startTime = System.currentTimeMillis();

            for(;;){
                dataFromServer = (JSONObject) objectInputStream.readObject();
                writeResumeFile((double) dataFromServer.get(JsonConstants.KEYBLOCKNUMBER));
                bufferedOutputStream.write((byte[]) dataFromServer.get(JsonConstants.KEYDATA),0,bufferSize);
                bufferedOutputStream.flush();
                dataFromServer = null;
            }

        }catch (SocketTimeoutException e) {
            // Timeout store the received data
            System.out.println("Timeout...");
        }catch (EOFException e) {
            // End of the stream
            long endTime = System.currentTimeMillis();
            TimeUtil.timeOfOperation("Fault tolerant receiving",startTime,endTime);
        }
        catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (fileOutputStream != null) fileOutputStream.close();
            if (bufferedOutputStream != null) bufferedOutputStream.close();
            if (clientSocket != null) clientSocket.close();
        }
    }
    // Store the package number
    private void writeResumeFile(double packageNumber) {
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JsonConstants.KEYRESUME, packageNumber);
            customProtocol.writeJsonToFile(JsonConstants.RESUME_FILE, jsonObject);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // Return the stored package number
    private double getStoredAmountOfPackage() {
        JSONObject jsonObject = customProtocol.readJsonFromFile(JsonConstants.RESUME_FILE);
        return (double) jsonObject.get(JsonConstants.KEYRESUME);
    }
}

