package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{

    private int PORT = 6789;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;
    private ServerSocket welcomeSocket;

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
                Socket connectionSocket = welcomeSocket.accept();

                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                outToClient = new PrintWriter(connectionSocket.getOutputStream(), true); // autoFlush

                // Read line from client

                String clientSentence = inFromClient.readLine();

                // Print out to the console what the client said
                System.out.println("Client> " + clientSentence);
                System.out.println("Client ip = "
                        + connectionSocket.getInetAddress());

                // Reverse the sentence from client

                String reverseSentence = new StringBuilder(clientSentence)
                        .reverse().toString();
                System.out.println("Server> " + reverseSentence);
                // Sent the reversed sentence back to the client

                outToClient.println(reverseSentence);
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public static void main(String[] args) throws IOException {

        Server s = new Server();
        s.start();

    }

}

