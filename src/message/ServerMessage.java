package message;

import java.io.Serializable;

public class ServerMessage implements Serializable {
    private String message;
    private int packetSize;
    private double fileSize;

    public ServerMessage() {
        this.message = "Download";
        this.packetSize = 0;
        this.fileSize = 0.0;
    }

    public ServerMessage(String message, int packetSize, double fileSize) {
        this.message = message;
        this.packetSize = packetSize;
        this.fileSize = fileSize;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public String getMessage() {
        return message;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public String toString() {
        return "Servers message: "+ this.message + ", package size: " + this.packetSize + ", file size: " + this.fileSize;
    }
}
