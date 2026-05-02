package ts.jni;

import java.io.Serializable;

public class NativeLib {
    static {
        System.loadLibrary("trains");
    }

    public static native long initialize(String fileName, String binName, String dataFolder);

    public static native void destroy(long data);

    public static native Data[] getData(long pbfData, double lat, double lon, int stationCount);

    public static class Data implements Serializable {
        public String routeName;
        public String nextStop;
        public String prevStop;
        public int time;
        public String headsign;
        public int delay;
        public boolean canceled;
        public int previousDeparture;
        public int nextArrival;
        public double distance;
        public int timeDiff;

        public Data(String routeName, String nextStop, String prevStop, int time, String headsign, int delay,
                    boolean canceled, int previousDeparture, int nextArrival, double distance, int timeDiff) {
            this.routeName = routeName;
            this.nextStop = nextStop;
            this.time = time;
            this.headsign = headsign;
            this.delay = delay;
            this.prevStop = prevStop;
            this.previousDeparture = previousDeparture;
            this.canceled = canceled;
            this.nextArrival = nextArrival;
            this.distance = distance;
            this.timeDiff = timeDiff;
        }

        public Data(String error) {
            this.routeName = "Fehler";
            this.headsign = error;

        }

        public static String format(int time) {
            int seconds = time % 60;
            int minutes = ((time - seconds) / 60) % 60;
            int hours = (time - seconds - minutes * 60) / 60 / 60;
            return String.format("%02d:%02d", hours, minutes);
        }
    }
}