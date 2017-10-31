package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{
    // http://www.rgagnon.com/javadetails/java-0542.html
    private int PORT = 6789;
    private final String FILEPATH = "C:/Temp/Bible.txt";
    private final String FILE_TO_SEND = "C:/Temp/Bible.txt";

    private BufferedReader inFromClient;
    private BufferedWriter outToClient;
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;

    private FileInputStream fis;
    private BufferedInputStream bis;
    private OutputStream os;

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

                // Read line from client

                String clientSentence = inFromClient.readLine();

                if(clientSentence.equals("Download"))
                    send();


            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void send() throws IOException {

        try{
            System.out.println("Download has started...");

            File fileToSend = new File(FILE_TO_SEND);
            byte [] mybytearray  = new byte [(int)fileToSend.length()];
            fis = new FileInputStream(fileToSend);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray,0,mybytearray.length);
            os = connectionSocket.getOutputStream();
            System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
            os.write(mybytearray,0,mybytearray.length);
            os.flush();
            System.out.println("Done.");

        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bis != null) bis.close();
            if (os != null) os.close();
            if (connectionSocket!=null) connectionSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {

        Server s = new Server();
        s.start();

    }

}

