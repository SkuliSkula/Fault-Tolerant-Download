package server;

import message.ClientMessage;
import message.ServerMessage;
import org.json.simple.JSONObject;
import utility.CustomProtocol;
import utility.JsonConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server extends Thread{
    private int PORT = 6789;
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private boolean interrupt;
    private final int PACKET_SIZE = 15000;
    private Logger logger = Logger.getLogger("Server");
    private ObjectOutputStream objectOutputStream;
    private String filePath;
    public Server(boolean interrupt, String filePath) {
        this.interrupt = interrupt;
        this.filePath = filePath;
        try {
            System.out.println("Starting server...");
            welcomeSocket = new ServerSocket(PORT);
            constructLogger();
        }catch (IOException e) {
            e.printStackTrace();
        }
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

                if(fromClient.get(JsonConstants.KEYREQUEST).equals(JsonConstants.VALUEREQUESTFILE)) { // New download
                    sendFile(0);
                }
                else if (fromClient.get(JsonConstants.KEYREQUEST).equals(JsonConstants.VALUEREQUESTBLOCK)) { // Resume download
                    sendFile((double)fromClient.get(JsonConstants.KEYBLOCKNUMBER));
                }

            }catch (IOException e) {
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                System.out.println("Could not find custom message: " + e.getLocalizedMessage());
            }

        }
    }

    private void resumeFile(double packageAlreadyReceived) {

    }

    private void sendFile(double packageAlreadyReceived) throws IOException {

        try{
            logger.info("Send file to client, package already received: " + packageAlreadyReceived);
            File fileToSend = new File(filePath);
            double noOfPackets=Math.ceil(((fileToSend.length())/PACKET_SIZE));
            logger.info("No of packets to send:" + noOfPackets);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
            long startTime = System.currentTimeMillis();
            logger.info("startTime: " + startTime);

            CustomProtocol sendToClient = new CustomProtocol();
            sendToClient.fileResponse(filePath);
            objectOutputStream.writeObject(sendToClient.getOverhead());

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
            timeOfOperation(startTime,endTime);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connectionSocket!=null) connectionSocket.close();
        }
    }

    private void showDownloadStatus(double i, double noOfPackets) {
        System.out.println(Math.ceil((100*(i/noOfPackets))) + "%");
    }

    private void timeOfOperation(long startTime, long endTime) {
        long timeElapsed = endTime - startTime;
        double seconds =  timeElapsed / 1000.0;
        logger.info("Time: "  + seconds + " seconds");
        logger.info("Download finished...");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String FILEPATH_BIBLE = "C:/Temp/Bible.txt";
        String FILE_LARGE = "C:/Temp/MrRobot.mkv";
        String FILE_TEST = "C/:Temp/Test.txt";
        Server s = new Server(false, FILE_LARGE);
        s.start();
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

}

