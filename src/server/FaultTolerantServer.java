package server;

import org.json.simple.JSONObject;
import utility.CustomProtocol;
import utility.JsonConstants;
import utility.TimeUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class FaultTolerantServer extends Thread{
    private int PORT = 6789;
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private boolean interrupt;
    private int packetSize;
    private ObjectOutputStream objectOutputStream;
    private boolean resume = false;
    private ArrayList<String> listOfFiles;
    private final String fileStorageDirectory = "C:/Temp/FaultTolerant/";
    private String fileName;
    public FaultTolerantServer(boolean interrupt) {
        this.interrupt = interrupt;
        constructFileList();
        try {
            welcomeSocket = new ServerSocket(PORT);
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

    public synchronized void  run() {

        while (true) {
            try{
                // Waits for a client to contact this socket
                connectionSocket = welcomeSocket.accept();

                // Create the streams to send and receive Message objects from and to the client
                objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());


                JSONObject fromClient = (JSONObject) objectInputStream.readObject();
                // Check if the file exists on the server
                if(checkIfFileExists((String)fromClient.get(JsonConstants.KEYFILE))) {
                    fileName = (String) fromClient.get(JsonConstants.KEYFILE);
                    packetSize = (int) fromClient.get(JsonConstants.KEYBLOCKSIZE);
                    if(fromClient.get(JsonConstants.KEYREQUEST).equals(JsonConstants.VALUEREQUESTFILE)) { // New download
                        resume = false;
                        sendFile(0);

                    }
                    else if (fromClient.get(JsonConstants.KEYREQUEST).equals(JsonConstants.VALUEREQUESTBLOCK)) { // Resume download
                        resume = true;
                        packetSize = (int) fromClient.get(JsonConstants.KEYBLOCKSIZE);
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
            File fileToSend = new File(fileStorageDirectory + fileName);
            double noOfPackets=Math.ceil(((fileToSend.length())/packetSize));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
            CustomProtocol sendToClient = new CustomProtocol();
            if(!resume){
                sendToClient.fileResponse(fileStorageDirectory + fileName, noOfPackets);
                objectOutputStream.writeObject(sendToClient.getOverhead());
            }
            System.out.println("Fault tolerant download started...");
            long startTime = System.currentTimeMillis();
            for(double i=0;i<noOfPackets + 1;i++) {
                byte[] byteArray = new byte[packetSize];
                bis.read(byteArray, 0, byteArray.length);
                sendToClient.simpleSend(byteArray,i);
                showDownloadStatus(i,noOfPackets);
                // Validate if the package should be sent or not
                if(!(i <= packageAlreadyReceived && packageAlreadyReceived != 0)) {
                    objectOutputStream.writeObject(sendToClient.getOverhead());
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
            TimeUtil.timeOfOperation("Fault tolerant download done",startTime,endTime);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connectionSocket!=null) connectionSocket.close();
        }
    }

    private void showDownloadStatus(double i, double noOfPackets) {
        System.out.println(Math.ceil((100*(i/noOfPackets))) + "%");
    }

}

