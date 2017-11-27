package client;

import org.json.simple.JSONObject;
import utility.CustomProtocol;
import utility.JsonConstants;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client extends Thread {

    private int PORT = 6789;
    private String HOST = "localhost";
    private final String FILE_TO_RECEIVE = "C:/Temp/Test/";

    private int command;
    private BufferedReader inFromUser;
    private Socket clientSocket;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;

    private int bufferSize;
    private double packageToReceive;
    private byte[] resumeData;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private CustomProtocol customProtocol;
    private String fileName;

    private JSONObject dataFromServer;
    public Client() {

        try{
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            packageToReceive = 0;
            bufferSize = 25000; // Default buffer size
            resumeData = null;
            customProtocol = new CustomProtocol();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {

        do {
            try {
                menu();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }while (command != 2);
    }

    private void requestDownload() throws IOException {

        try {
            clientSocket = new Socket(HOST,PORT);
            System.out.println("Connecting...");

            // Check how many package we already received
            double savedNoPackage = getStoredAmountOfPackage();
            System.out.println("Number of package already received: " + savedNoPackage);

            // Create the streams to send and receive Message objects
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            if(savedNoPackage == 0.0) {
                customProtocol.fileRequest("C:/Temp/MrRobot.mkv");
                objectOutputStream.writeObject(customProtocol.getOverhead());
                // Get message back from the server
                JSONObject fromServer = (JSONObject) objectInputStream.readObject();
                customProtocol.writeJsonToFile(JsonConstants.CONFIG_FILE, fromServer);
                //Calculate how many package the client will receive
                fileName = (String) fromServer.get(JsonConstants.KEYFILE);
                long fileSize = (long) fromServer.get(JsonConstants.KEYFILESIZE);
                long bufferSizeLong = (long) fromServer.get(JsonConstants.KEYBLOCKSIZE);
                //bufferSize = toIntExact(bufferSizeLong);
                packageToReceive = Math.ceil(fileSize / bufferSize);
                System.out.println("Package to receive: " + packageToReceive);

                receiveFile(fileName);
            }
            else{
                customProtocol.blockRequest("C:/Temp/MrRobot.mkv",0, savedNoPackage);
                objectOutputStream.writeObject(customProtocol.getOverhead());
                resumeFile();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }/*finally {
            System.out.println("Finally in requestDownload...");
            objectOutputStream.flush();
            objectOutputStream.close();
            objectInputStream.close();
        }*/
    }
    // We resume the download, we read the bytes back into memory from where it crashed
    private void resumeFile(){
        JSONObject jsonObject = customProtocol.readJsonFromFile(JsonConstants.CONFIG_FILE);
        String filePath = (String) jsonObject.get(JsonConstants.KEYFILE);
        System.out.println("Resume download");
        Path fileLocation = Paths.get(FILE_TO_RECEIVE + filePath);
        try{
            resumeData = Files.readAllBytes(fileLocation);
            //After the bytes have been stored we continue to receiving the file
            receiveFile(filePath);
        }catch (IOException e) {
            System.out.println("Failed to resume the download: " + e.getLocalizedMessage());
        }

    }

    private void receiveFile(String fileName) throws IOException {
            System.out.println("receiveFile...");
        try{
            fileOutputStream = new FileOutputStream(FILE_TO_RECEIVE+fileName);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            // Write the resume data first to the file
            if(resumeData != null)
                bufferedOutputStream.write(resumeData);

            for(;;){
                dataFromServer = (JSONObject) objectInputStream.readObject();
                bufferedOutputStream.write((byte[]) dataFromServer.get(JsonConstants.KEYDATA),0,bufferSize);
            }

        }catch (SocketTimeoutException e) {
            // Timeout store the received data
            System.out.println("Timeout...");
            writeResumeFile((int)dataFromServer.get(JsonConstants.KEYBLOCKNUMBER));
        }catch (EOFException e) {
            // End of the stream
            System.out.println("End of stream...");
            writeResumeFile((double)dataFromServer.get(JsonConstants.KEYBLOCKNUMBER));
        }
        catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        /*finally {
            if (fileOutputStream != null) fileOutputStream.close();
            if (bufferedOutputStream != null) bufferedOutputStream.close();
            if (clientSocket != null) clientSocket.close();
        }*/
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
    // The options displayed to the client
    private void menu() throws IOException {
        System.out.println();
        System.out.println("Welcome... Select an option to continue");
        System.out.println("1: Download a file");
        System.out.println("2: Disconnect");
        System.out.println();
        command = Integer.parseInt(inFromUser.readLine());

        switch (command) {
            case 1:
                requestDownload();
                break;
            case 2:
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) throws IOException {
            Client c = new Client();
            c.start();

    }
}

