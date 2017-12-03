package client;

import message.ClientMessage;
import message.ServerMessage;
import org.json.simple.JSONObject;
import utility.CustomProtocol;
import utility.TimeUtil;

import java.io.*;
import java.net.Socket;

public class RegularClient extends Thread{

    private int PORT = 56000;
    private String HOST = "localhost";
    private final String FILE_TO_RECEIVE = "C:/Temp/Test/";

    private int bufferSize;
    private BufferedReader inFromUser;
    private Socket clientSocket;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;
    private String requestFileName;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public RegularClient(String requestFileName){
        this.requestFileName = requestFileName;
        try{
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            bufferSize = 25000; // Default buffer size
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void run() {
        try {
            requestDownload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestDownload() throws IOException{

        try{
            clientSocket = new Socket(HOST,PORT);
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            objectOutputStream.writeObject(new ClientMessage("Download", 0, requestFileName));
            ServerMessage serverMessage = (ServerMessage) objectInputStream.readObject();
            if(serverMessage.getMessage().equals("Download")) {
                receiveFile();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(objectInputStream != null)
                objectInputStream.close();
            if(objectOutputStream != null) {
                objectOutputStream.flush();
                objectOutputStream.close();
            }
        }

    }

    private void receiveFile() throws IOException{

        try{
            InputStream inputStream = clientSocket.getInputStream();
            fileOutputStream = new FileOutputStream(FILE_TO_RECEIVE + "Regular" + requestFileName);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            int count;
            byte[] buffer = new byte[bufferSize];
            long startTime = System.currentTimeMillis();
            while ((count = inputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer,0,count);
            }
            long endTime = System.currentTimeMillis();
            TimeUtil.timeOfOperation("Regular receiving",startTime,endTime);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fileOutputStream != null) fileOutputStream.close();
            if (clientSocket != null) clientSocket.close();
        }
    }
}
