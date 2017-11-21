package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{
    private int PORT = 6789;
    private final String FILEPATH = "C:/Temp/Bible.txt";
    private final String FILE_TO_SEND = "C:/Temp/MrRobot.mkv";

    private BufferedReader inFromClient;
    //private BufferedWriter outToClient;
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private PrintWriter outToClient;

    private final int PACKET_SIZE = 120000;

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

                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                outToClient= new PrintWriter(connectionSocket.getOutputStream(), true); // autoFlush
                // Read line from client

                String clientSentence = inFromClient.readLine();

                if(clientSentence.equals("Download")) {
                    File fileToSend = new File(FILE_TO_SEND);
                    String fileSize = Long.toString(fileToSend.length());
                    outToClient.println(fileSize);
                    sendFile();
                }

            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendFile() throws IOException {

        try{
            System.out.println("Download has started...");
            File fileToSend = new File(FILE_TO_SEND);
            double nosofpackets=Math.ceil((fileToSend.length())/PACKET_SIZE);
            System.out.println("No of packets to send:" + nosofpackets);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
            long startTime = System.currentTimeMillis();
            for(double i=0;i<nosofpackets+1;i++) {
                byte[] bytearray = new byte[PACKET_SIZE];
                bis.read(bytearray, 0, bytearray.length);
                System.out.println("Packet:"+(i+1));
                OutputStream os = connectionSocket.getOutputStream();
                os.write(bytearray, 0,bytearray.length);
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

}

