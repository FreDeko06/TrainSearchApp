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

        @Override
        public String toString() {
            int now = LocalDateTime.now().toLocalTime().toSecondOfDay();
            int delta = time - now;
            String absoluteTime = format(time);
            String deltaTime = format(Math.abs(delta));
            if (delta < 0) {
                deltaTime = "+ " + deltaTime;
            }else {
                deltaTime = "- " + deltaTime;
            }
            return String.format("%s Uhr: %s        %s\nnach %s", absoluteTime, routeName, deltaTime, nextStop);
        }

        private String format(int time) {
            int seconds = time % 60;
            int minutes = ((time - seconds) / 60) % 60;
            int hours = (time - seconds - minutes * 60) / 60 / 60;
            return String.format("%02d:%02d", hours, minutes);
        }
    }
}