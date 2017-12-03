package server;

import org.json.simple.JSONObject;
import utility.ChecksumUtil;
import utility.CustomProtocol;
import utility.JsonConstants;
import utility.TimeUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server extends Thread{
    private int PORT = 6789;
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private boolean interrupt;
    private final int PACKET_SIZE = 25000;
    private Logger logger = Logger.getLogger("Server");
    private ObjectOutputStream objectOutputStream;
    private boolean resume = false;
    private ArrayList<String> listOfFiles;
    private final String fileStorageDirectory = "C:/Temp/";
    private String fileName;
    public Server(boolean interrupt) {
        this.interrupt = interrupt;
        constructFileList();
        try {
            System.out.println("Starting server...");
            welcomeSocket = new ServerSocket(PORT);
            constructLogger();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void constructFileList() {
        listOfFiles = new ArrayList<>();
        listOfFiles.add("Bible.txt");
        listOfFiles.add("MrRobot.mkv");
        listOfFiles.add("test.mp4");
    }

    private boolean checkIfFileExists(String fileName) {
        return listOfFiles.contains(fileName);
    }

    public void run() {

        while (true) {
            System.out.println("Waiting for a client...");
            logger.info("Waiting for a client");
            try{
                // Waits for a client to contact this socket
                connectionSocket = welcomeSocket.accept();
                logger.info("Client connected: " + connectionSocket);

                // Create the streams to send and receive Message objects from and to the client
                objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());


                JSONObject fromClient = (JSONObject) objectInputStream.readObject();
                // Check if the file exists on the server
                if(checkIfFileExists((String)fromClient.get(JsonConstants.KEYFILE))) {
                    fileName = (String) fromClient.get(JsonConstants.KEYFILE);
                    if(fromClient.get(JsonConstants.KEYREQUEST).equals(JsonConstants.VALUEREQUESTFILE)) { // New download
                        resume = false;
                        sendFile(0);
                    }
                    else if (fromClient.get(JsonConstants.KEYREQUEST).equals(JsonConstants.VALUEREQUESTBLOCK)) { // Resume download
                        resume = true;
                        sendFile((double)fromClient.get(JsonConstants.KEYBLOCKNUMBER));
                    }
                }else {
                    System.out.println("The requested file does not exist on the server: " + fileName);
                }

            }catch (IOException e) {
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                System.out.println("Could not find custom message: " + e.getLocalizedMessage());
            }

        }
    }

    private void sendFile(double packageAlreadyReceived) throws IOException {

        try{
            logger.info("Send file to client, package already received: " + packageAlreadyReceived);
            File fileToSend = new File(fileStorageDirectory + fileName);
            double noOfPackets=Math.ceil(((fileToSend.length())/PACKET_SIZE));
            logger.info("No of packets to send:" + noOfPackets);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
            String md5 = ChecksumUtil.getFileCheckSum(fileName, fileStorageDirectory);
            System.out.println("Md5 = " + md5);
            CustomProtocol sendToClient = new CustomProtocol();
            if(!resume){
                sendToClient.fileResponse(fileStorageDirectory + fileName, noOfPackets);
                objectOutputStream.writeObject(sendToClient.getOverhead());
            }
            long startTime = System.currentTimeMillis();
            for(double i=0;i<noOfPackets+1;i++) {
                byte[] byteArray = new byte[PACKET_SIZE];
                bis.read(byteArray, 0, byteArray.length);
                sendToClient.simpleSend(byteArray,i);
                showDownloadStatus(i, noOfPackets);
                // If packageAlreadyReceived is less then current package then don't send them
                if(i <= packageAlreadyReceived && packageAlreadyReceived != 0) {
                    logger.info("Package already received: " + i);
                }else{
                    objectOutputStream.writeObject(sendToClient.getOverhead());
                    logger.info("Send me package: " + i);
                }

                // Fake connection interruption (selected in the constructor)
                if(interrupt) {
                    if((int)i == (int)noOfPackets/2){
                        connectionSocket.close();
                    }
                }

                objectOutputStream.flush();
            }
            long endTime = System.currentTimeMillis();
            TimeUtil.timeOfOperation("Server sending file",startTime,endTime);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connectionSocket!=null) connectionSocket.close();
        }
    }

    private void showDownloadStatus(double i, double noOfPackets) {
        System.out.println(Math.ceil((100*(i/noOfPackets))) + "%");
    }



    private void constructLogger() {
        FileHandler fh;
        try {
            fh = new FileHandler("C:/Temp/log/server.log");
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server s = new Server(false);
        s.start();
    }
}

