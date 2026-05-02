package ts.jni;

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
        public String prevStop;
        public int time;
        public String headsign;
        public int delay;
        public boolean canceled;
        public int previousDeparture;
        public int nextArrival;

        public Data(String routeName, String nextStop, String prevStop, int time, String headsign, int delay,
                    boolean canceled, int previousDeparture, int nextArrival) {
            this.routeName = routeName;
            this.nextStop = nextStop;
            this.time = time;
            this.headsign = headsign;
            this.delay = delay;
            this.prevStop = prevStop;
            this.previousDeparture = previousDeparture;
            this.canceled = canceled;
            this.nextArrival = nextArrival;
        }

        public Data(String error) {
            this.routeName = "Fehler";
            this.headsign = error;

        }
    }
}