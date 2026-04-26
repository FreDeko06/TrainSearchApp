package ts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ts.jni.NativeLib;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            TextView mapProgress = findViewById(R.id.mapProgress);
            TextView timeProgress = findViewById(R.id.timeProgress);
            TextView status = findViewById(R.id.textStatus);
            mapProgress.setText("");
            timeProgress.setText("");
            status.setText("Lade Daten...");
            Downloader pbf = new Downloader(new URL("http://192.168.0.102/rail.pbf"), new File(getFilesDir(), "rail.pbf"));
            pbf.onFinish((s) -> {
                runOnUiThread(() -> mapProgress.setText("Fertig!"));
            });
            File gtfsFile = new File(getFilesDir(), "latest.zip");
            Downloader gtfs = new Downloader(new URL("https://download.gtfs.de/germany/free/latest.zip"), gtfsFile);
            new Thread(() -> {
                long data = init();
                if (data == 0) {
                    // we have to download data
                    runOnUiThread(() -> {
                        pbf.printStatus(MainActivity.this, mapProgress);
                        gtfs.printStatus(MainActivity.this, timeProgress);
                        status.setText("Lade Daten herunter...");
                    });
                    pbf.start();
                    gtfs.start();
                    try {
                        runOnUiThread(() -> timeProgress.setText("Entpacken..."));
                        unzip(gtfsFile, new File(getFilesDir(), "time_data"));
                        runOnUiThread(() -> timeProgress.setText("Fertig!"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> timeProgress.setText("Entpacken fehlgeschlagen :("));
                    }
                }

                runOnUiThread(() -> status.setText("Lade Daten..."));
                data = init();


                if (data != 0) {
                    runOnUiThread(() -> status.setText("Bitte warten..."));
                } else {
                    runOnUiThread(() -> status.setText("Fehler! :("));
                }

                new File(getFilesDir(), "rail.pbf").delete();
                File times = new File(getFilesDir(), "time_data");
                if (times.isDirectory()) {
                    for (File f : times.listFiles()) {
                        f.delete();
                    }
                    times.delete();
                }


                if (data != 0) {
                    Intent myIntent = new Intent(MainActivity.this, MainPage.class);
                    myIntent.putExtra("data", data);
                    MainActivity.this.startActivity(myIntent);
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private long init() {
        try {
            return NativeLib.initialize(new File(getFilesDir(), "rail.pbf").getAbsolutePath(), new File(getFilesDir(), "data.bin").getAbsolutePath(), new File(getFilesDir(), "time_data").getAbsolutePath());
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void unzip(File zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[4096];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }

                outFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                zis.closeEntry();
            }
        }
    }


}