package client;

import java.io.*;
import java.net.Socket;

public class Client extends Thread {

    private int PORT = 6789;
    private String HOST = "localhost";
    private final String FILE_TO_RECEIVED = "C:/Temp/testClient.mkv";
    private int command ;
    private BufferedReader inFromUser;
    private BufferedReader inFromServer;
    private Socket clientSocket;
    private PrintWriter outToServer;
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    private final int BUFFER_SIZE = 15000;

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

            outToServer = new PrintWriter(clientSocket.getOutputStream(),true); // true = autoFlush
            outToServer.println("Download");

            String serverResponse = inFromServer.readLine();
            System.out.println("File size:" + serverResponse);
            receiveFile();


        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() throws IOException {
            System.out.println("receiveFile...");
        try{
            InputStream is = clientSocket.getInputStream();
            fos = new FileOutputStream(FILE_TO_RECEIVED);
            bos = new BufferedOutputStream(fos);
            int count;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((count = is.read(buffer)) > 0)
            {
                bos.write(buffer, 0, count);
                System.out.println("Count: " + count);
            }

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

