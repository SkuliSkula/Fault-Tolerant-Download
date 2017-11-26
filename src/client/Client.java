package client;

import message.ClientMessage;
import message.ServerMessage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Client extends Thread {

    private int PORT = 6789;
    private String HOST = "localhost";
    private final String FILE_TO_RECEIVE = "C:/Temp/Test/MrRobot.mkv";
    private final String FILE_RESUME = "C:/Temp/resume.txt";

    private int command;
    private BufferedReader inFromUser;
    private Socket clientSocket;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;

    private int bufferSize;
    private double packageToReceive;
    private byte[] resumeData;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public Client() {

        try{
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            packageToReceive = 0;
            bufferSize = 12000; // Default buffer size
            resumeData = null;
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

            // Check how many package we already received
            double savedNoPackage = readResumeFile();
            System.out.println("Number of package already received: " + savedNoPackage);

            // Create the streams to send and receive Message objects
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            // Send to the server that you want to download and how many package you have received
            objectOutputStream.writeObject(new ClientMessage("Download", savedNoPackage));

            // Get message back from the server
            ServerMessage m = (ServerMessage) objectInputStream.readObject();
            // Show what the server sent
            System.out.println(m.toString());
            //Calculate how many package the client will receive
            double fileSize = m.getFileSize();
            bufferSize = m.getPacketSize();
            packageToReceive = Math.ceil(fileSize/bufferSize);
            System.out.println("Package to receive: " + packageToReceive);
            // Do some action bases on servers message
            if(m.getMessage().equals("Resume"))
                resumeFile();
            else if(m.getMessage().equals("NewDownload"))
                receiveFile();

        }catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            System.out.println("Finally in requestDownload...");
            objectOutputStream.flush();
            objectOutputStream.close();
            objectInputStream.close();
        }
    }
    // We resume the download, we read the bytes back into memory from where it crashed
    private void resumeFile(){
        System.out.println("Resume download");
        Path fileLocation = Paths.get(FILE_TO_RECEIVE);
        try{
            resumeData = Files.readAllBytes(fileLocation);
            //After the bytes have been stored we continue to receiving the file
            receiveFile();
        }catch (IOException e) {
            System.out.println("Failed to resume the download: " + e.getLocalizedMessage());
        }

    }

    private void receiveFile() throws IOException {
            System.out.println("receiveFile...");
        try{
            InputStream is = clientSocket.getInputStream();
            fileOutputStream = new FileOutputStream(FILE_TO_RECEIVE);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            // Write the resume data first to the file
            if(resumeData != null)
                bufferedOutputStream.write(resumeData);

            int count;
            int counter = 0;
            byte[] buffer = new byte[bufferSize];
            while ((count = is.read(buffer)) > 0)
            {
                bufferedOutputStream.write(buffer, 0, count);
                 counter++;
            }
            if(counter < packageToReceive)
                writeResumeFile(counter);
        }catch (IOException e) {
            System.out.println("Receive file failed: " + e.getLocalizedMessage());
        }
        finally {
            if (fileOutputStream != null) fileOutputStream.close();
            if (bufferedOutputStream != null) bufferedOutputStream.close();
            if (clientSocket != null) clientSocket.close();
        }
    }
    // Store the package number
    private void writeResumeFile(double packageNumber) {
        try{
            Files.write(Paths.get(FILE_RESUME), Double.toString(packageNumber).getBytes());
            System.out.println(packageNumber +" package stored in the file...");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    // Return the stored package number
    private double readResumeFile() {
        String results = "";
        byte[] arr;
        try{
            arr = Files.readAllBytes(Paths.get(FILE_RESUME));
            results = new String(arr);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return Double.parseDouble(results);
    }
    // The options displayed to the client
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

