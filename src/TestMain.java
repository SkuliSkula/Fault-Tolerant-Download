import client.FaultTolerantClient;
import client.RegularClient;
import server.FaultTolerantServer;
import server.RegularServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestMain {

    public static void main(String[] args) throws IOException {
        String FILE_BIBLE = "Bible.txt";
        String FILE_LARGE = "MrRobot.mkv";
        String FILE_500 = "test.mp4";
        String FILE_TEST = "Test.txt";
        String fileName = "Bible.txt";

        /*Thread regularServer = new Thread(new Runnable(){
           public void run(){
               RegularServer regularServer = new RegularServer();
               regularServer.start();
               RegularClient regularClient = new RegularClient("MrRobot.mkv");
               regularClient.start();
           }
        });
        regularServer.start();*/

        Thread faultTolerantServer = new Thread(new Runnable(){
            public void run(){
                FaultTolerantServer faultTolerantServer = new FaultTolerantServer(false);
                faultTolerantServer.start();
                FaultTolerantClient faultTolerantClient = new FaultTolerantClient("test.mp4", 10000);
                faultTolerantClient.start();
            }
        });
        faultTolerantServer.start();
    }
}
