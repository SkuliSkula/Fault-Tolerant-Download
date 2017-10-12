package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    /**
     * Create a protocol for the server The client sends a sentence and the
     * server reverses the sentence
     *
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {

        final int port = 6789;
        System.out.println("Starting server...");

        // Create the welcome socket

        ServerSocket welcomeSocket = new ServerSocket(port);

        while (true) {
            System.out.println("Waiting for a client...");

            // Waits for a client to contact this socket
            Socket connectionSocket = welcomeSocket.accept();

            // Create input stream that is attached to the socket

            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));

            // Create output stream that is attached to the socket

            PrintWriter outToClient = new PrintWriter(
                    connectionSocket.getOutputStream(), true); // autoFlush

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

        }

    }

}

