package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {

    private int PORT = 6789;
    private String HOST = "localhost";
    private int command ;
    private BufferedReader inFromUser;
    private BufferedReader inFromServer;
    private Socket clientSocket;
    private PrintWriter outToServer;

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

            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer = new PrintWriter(clientSocket.getOutputStream());
            outToServer = new PrintWriter(clientSocket.getOutputStream(),true); // true = autoFlush
            outToServer.println("Download");

            String reversedSentence = inFromServer.readLine();
            System.out.println("Server> " + reversedSentence);

        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("Closing client socket...");
            clientSocket.close();
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

