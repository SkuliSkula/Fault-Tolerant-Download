package utility;

public class TimeUtil {

    public static void timeOfOperation(String message, long startTime, long endTime) {
        long timeElapsed = endTime - startTime;
        double seconds =  timeElapsed / 1000.0;
        System.out.println(message + " time: "  + seconds + " seconds");
    }
}
