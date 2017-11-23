package client;

import message.MyMessage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client extends Thread {

    private int PORT = 6789;
    private String HOST = "localhost";
    private final String FILE_TO_RECEIVE = "C:/Temp/testClient.txt";
    private final String FILE_RESUME = "C:/Temp/resume.txt";

    private int command ;
    private BufferedReader inFromUser;
    private BufferedReader inFromServer;
    private Socket clientSocket;
    private PrintWriter outToServer;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;

    private final int BUFFER_SIZE = 12000;
    private double packageToReceive;
    private byte[] resumeData;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public Client() {

        try{
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            packageToReceive = 0;
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
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            double savedNoPackage = readResumeFile();
            System.out.println("Number of package already received: " + savedNoPackage);
            outToServer = new PrintWriter(clientSocket.getOutputStream(),true); // true = autoFlush

            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            // Send to the server that you want to download and how many package you have received
            objectOutputStream.writeObject(new MyMessage("Download", savedNoPackage));

            // Get message back from the server
            MyMessage m = (MyMessage) objectInputStream.readObject();
            packageToReceive = m.getPackageReceived();

            if(m.getOperation().equals("Resume"))
                resumeFile();
            else if(m.getOperation().equals("NewDownload"))
                receiveFile();

        }catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((count = is.read(buffer)) > 0)
            {
                bufferedOutputStream.write(buffer, 0, count);
                System.out.println("Counter: " + counter++);
            }
            if(counter <packageToReceive)
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

