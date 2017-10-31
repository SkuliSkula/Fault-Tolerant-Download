package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client extends Thread {

    private int PORT = 6789;
    private String HOST = "localhost";
    private final String FILE_TO_RECEIVED = "C:/Temp/testClient.txt";
    private int command ;
    private BufferedReader inFromUser;
    private BufferedReader inFromServer;
    private Socket clientSocket;
    private PrintWriter outToServer;

    private final int FILE_SIZE = 6022386;
    private int bytesRead;
    private int current = 0;
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    public Client() {

        try{
            inFromUser = new BufferedReader(new InputStreamReader(System.in));

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
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer = new PrintWriter(clientSocket.getOutputStream());
            outToServer = new PrintWriter(clientSocket.getOutputStream(),true); // true = autoFlush
            outToServer.println("Download");
            receiveFile();


        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("Closing client socket...");
            clientSocket.close();
        }


    }

    private void receiveFile() throws IOException {
            System.out.println("receiveFile...");
        try{
            byte [] mybytearray  = new byte [FILE_SIZE];
            InputStream is = clientSocket.getInputStream();
            fos = new FileOutputStream(FILE_TO_RECEIVED);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;

            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead < -1);

            bos.write(mybytearray, 0 , current);
            bos.flush();
            System.out.println("File " + FILE_TO_RECEIVED
                    + " downloaded (" + current + " bytes read)");

        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fos != null) fos.close();
            if (bos != null) bos.close();
            if (clientSocket != null) clientSocket.close();
        }

    }

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

