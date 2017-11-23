package server;

import message.ClientMessage;
import message.ServerMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server extends Thread{
    private int PORT = 6789;
    private final String FILEPATH_BIBLE = "C:/Temp/Bible.txt";
    private final String FILE_LARGE = "C:/Temp/MrRobot.mkv";
    private final String FILE_TEST = "C/:Temp/Test.txt";
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private boolean resume = false;
    private final int PACKET_SIZE = 12000;
    private Logger logger = Logger.getLogger("Server");
    public Server() {
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
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());

                // Get the message from the client
                ClientMessage m = (ClientMessage) objectInputStream.readObject();
                System.out.println(m.toString());
                logger.info(m.toString());
                // Check what the message contains and do operations based on that
                if(m.getOperation().equals("Download")) {
                    if(m.getPackageReceived()>0)
                        resume = true;
                    // Send a message to client, resume or new download request, the packet size being sent and the file size
                    objectOutputStream.writeObject(new ServerMessage(resume?"Resume":"NewDownload", PACKET_SIZE, getFileSize(FILE_LARGE)));
                    sendFile(m.getPackageReceived());
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
            File fileToSend = new File(FILE_LARGE);
            double noOfPackets=Math.ceil(((fileToSend.length())/PACKET_SIZE));
            logger.info("No of packets to send:" + noOfPackets);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
            long startTime = System.currentTimeMillis();
            logger.info("startTime: " + startTime);
            for(double i=0;i<noOfPackets+1;i++) {
                byte[] byteArray = new byte[PACKET_SIZE];
                bis.read(byteArray, 0, byteArray.length);
                //System.out.println("Packet:"+(i+1));
                OutputStream os = connectionSocket.getOutputStream();
                showDownloadStatus(i, noOfPackets);
                // If packageAlreadyReceived is less then current package then don't send them
                if(i < packageAlreadyReceived) {
                    logger.info("Package already received: " + i);
                }else{
                    os.write(byteArray, 0,byteArray.length);
                }


                /*if(i == (noOfPackets/2)){
                    connectionSocket.close();
                    os.flush();
                    os.close();
                }*/
                os.flush();
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
        System.out.print(Math.ceil((100*(i/noOfPackets))) + "%");
        System.out.flush();
        logger.info("Download status: " + Math.ceil((100*(i/noOfPackets))) + "%");
    }

    private void timeOfOperation(long startTime, long endTime) {
        long timeElapsed = endTime - startTime;
        double seconds =  timeElapsed / 1000.0;
        logger.info("Time: "  + seconds + " seconds");
        logger.info("Download finished...");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server s = new Server();
        s.start();
    }

    private long getFileSize(String filePath){
        File fileToSend = new File(filePath);
        return fileToSend.length();

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

