package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    /**
     * Creating a protocol for the client the client sends a sentence to a
     * server and the server reverses the sentence and sends it back
     *
     * @param args
     * @throws UnknownHostException
     * @throws IOException
     */

    public static void main(String[] args) throws UnknownHostException, IOException {

        final int PORT = 6789;
        final String HOST = "localhost";

        // Create a input stream (From the keyboard)

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        // Create a client socket

        Socket clientSocket = new Socket(HOST,PORT);

        // Create input stream attached to the client socket

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Create output stream attached to the client socket

        PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(),true); // true = autoFlush

        // Read a sentence from the user

        System.out.println("Write a line to the server: ");
        String sentence = inFromUser.readLine();
        System.out.println("Client> " + sentence);

        // Send the line to the server

        outToServer.println(sentence);

        // Read the reversed sentence from the server

        String reversedSentence = inFromServer.readLine();
        System.out.println("Server> " + reversedSentence);

        // Close the connection
        clientSocket.close();

    }
}

