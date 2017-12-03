package message;

import java.io.Serializable;

public class ClientMessage implements Serializable {
    private String operation;
    private double packageReceived;
    private String fileName;

    public ClientMessage(String operation, double packageReceived, String fileName){
        this.operation = operation;
        this.packageReceived = packageReceived;
        this.fileName = fileName;
    }

    public double getPackageReceived() {
        return packageReceived;
    }

    public void setPackageReceived(double packageReceived) {
        this.packageReceived = packageReceived;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String toString(){
        return "Operation: " + this.operation + ", packageReceived: " + packageReceived;
    }
}
