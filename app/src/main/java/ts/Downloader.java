package ts;

import android.os.Handler;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.function.Consumer;

public class Downloader {

    private URL url;
    private File dest;
    private long total;
    private Consumer<Long> finished;
    private boolean isFinished = false;

    public Downloader(URL url, File dest) {
        this.url = url;
        this.dest = dest;
    }

    public void onFinish(Consumer<Long> onFinish) {
        finished = onFinish;
    }

    public void printStatus(MainActivity activity, TextView text) {
        Handler h = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinished) return;
                        text.setText(getStatus());
                        h.postDelayed(this, 1000);
                    }
                });
            }
        };

        h.postDelayed(task, 1000);

    }

    public String getStatus() {
        return String.format("%.2f MiB", (float)total/1024.0/1024.0);
    }

    public void start() {
        total = 0;
        try (BufferedInputStream bir = new BufferedInputStream(url.openStream());
             FileOutputStream fos = new FileOutputStream(dest)){
            byte[] buffer = new byte[1024];
            int read;

            while((read = bir.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
                total += read;
            }
            isFinished = true;
            System.out.println("Downloading finished!");
            if (finished != null) {
                finished.accept(total);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startAsync() {
        isFinished = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();
    }

}
