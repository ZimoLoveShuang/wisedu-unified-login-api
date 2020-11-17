package wiki.zimo.wiseduunifiedloginapi.helper;

public class TimeStampHelper {
    public static String currentTimeSeconds() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }
}
