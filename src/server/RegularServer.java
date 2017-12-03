package server;

import message.ClientMessage;
import message.ServerMessage;
import utility.TimeUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RegularServer extends Thread{
    private int PORT = 56000;
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private final int PACKET_SIZE = 25000;
    private ObjectOutputStream objectOutputStream;
    private ArrayList<String> listOfFiles;
    private final String fileStorageDirectory = "C:/Temp/Regular/";
    private String fileName;

    public RegularServer() {
        constructFileList();
        try {
            welcomeSocket = new ServerSocket(PORT);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void run() {

        while (true) {
            try{
                connectionSocket = welcomeSocket.accept();

                objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());

                ClientMessage clientMessage = (ClientMessage) objectInputStream.readObject();

                if(clientMessage != null) {

                    if(clientMessage.getOperation().equals("Download")){
                        fileName = clientMessage.getFileName();
                        objectOutputStream.writeObject(new ServerMessage());
                        sendFile();
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void constructFileList() {
        listOfFiles = new ArrayList<>();
        listOfFiles.add("Bible.txt");
        listOfFiles.add("MrRobot.mkv");
        listOfFiles.add("test.mp4");
    }

    private void sendFile() throws IOException{

        try{
            File fileToSend = new File(fileStorageDirectory + fileName);
            double noOfPackets = Math.ceil((fileToSend.length()/PACKET_SIZE));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
            System.out.println("Regular download started...");
            long startTime = System.currentTimeMillis();
            for(double i = 0; i < noOfPackets + 1; i++) {
                byte[] byteArray = new byte[PACKET_SIZE];
                bis.read(byteArray,0,byteArray.length);
                OutputStream os = connectionSocket.getOutputStream();
                os.write(byteArray,0,byteArray.length);
                os.flush();
            }
            long endTime = System.currentTimeMillis();
            TimeUtil.timeOfOperation("Regular download done",startTime,endTime);
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connectionSocket!=null) connectionSocket.close();
        }
    }
}
