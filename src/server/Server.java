package server;

import message.MyMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Server extends Thread{
    private int PORT = 6789;
    private final String FILEPATH_BIBLE = "C:/Temp/Bible.txt";
    private final String FILE_LARGE = "C:/Temp/MrRobot.mkv";
    private final String FILE_TEST = "C/:Temp/Test.txt";
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private boolean resume = false;
    private final int PACKET_SIZE = 12000;

    public Server() {
        try {
            System.out.println("Starting server...");
            welcomeSocket = new ServerSocket(PORT);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {

        while (true) {
            System.out.println("Waiting for a client...");
            try{
                // Waits for a client to contact this socket
                connectionSocket = welcomeSocket.accept();
                System.out.println("Accepted connection : " + connectionSocket);

                objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());

                MyMessage m = (MyMessage) objectInputStream.readObject();
                System.out.println(m.toString());

                if(m.getOperation().equals("Download")) {
                    if(m.getPackageReceived()>0)
                        resume = true;
                    objectOutputStream.writeObject(new MyMessage(resume?"Resume":"NewDownload", getFileSize(FILEPATH_BIBLE)));
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
            System.out.println("Download has started...");
            File fileToSend = new File(FILEPATH_BIBLE);
            double noOfPackets=Math.ceil(((fileToSend.length())/PACKET_SIZE));
            System.out.println("No of packets to send:" + noOfPackets);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
            long startTime = System.currentTimeMillis();
            for(double i=0;i<noOfPackets+1;i++) {
                byte[] byteArray = new byte[PACKET_SIZE];
                bis.read(byteArray, 0, byteArray.length);
                System.out.println("Packet:"+(i+1));
                OutputStream os = connectionSocket.getOutputStream();
                if(i < packageAlreadyReceived) {// If packageAlreadyReceived is less then current package then don't send them
                    System.out.println("Package already received...");
                }else
                    os.write(byteArray, 0,byteArray.length);

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

    private void timeOfOperation(long startTime, long endTime) {
        long timeElapsed = endTime - startTime;
        double seconds =  timeElapsed / 1000.0;
        System.out.println("Time: "  + seconds + " seconds");
    }

    public static void main(String[] args) throws IOException {

        Server s = new Server();
        s.start();

    }

    private double getFileSize(String filePath){
        File fileToSend = new File(filePath);
        return Math.ceil(fileToSend.length()/PACKET_SIZE);

    }

}

