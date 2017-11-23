package message;

import java.io.Serializable;

public class MyMessage implements Serializable {
    private String operation;
    private double packageReceived;

    public MyMessage(String operation, double packageReceived){
        this.operation = operation;
        this.packageReceived = packageReceived;
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

    public String toString(){
        return "Operation: " + this.operation + ", packageReceived: " + packageReceived;
    }
}
