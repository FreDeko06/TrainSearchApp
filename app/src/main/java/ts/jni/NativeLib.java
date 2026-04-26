package ts.jni;

import java.time.LocalDateTime;

public class NativeLib {
    static {
        System.loadLibrary("trains");
    }

    public static native long initialize(String fileName, String binName, String dataFolder);

    public static native void destroy(long data);

    public static native Data[] getData(long pbfData, double lat, double lon, int stationCount);

    public static class Data {
        public String routeName;
        public String nextStop;
        public int time;
        public String headsign;

        public Data(String routeName, String nextStop, int time, String headsign) {
            this.routeName = routeName;
            this.nextStop = nextStop;
            this.time = time;
            this.headsign = headsign;
        }
    }
}