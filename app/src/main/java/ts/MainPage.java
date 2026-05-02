package ts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ts.jni.NativeLib;

public class MainPage extends AppCompatActivity {

    private DataAdapter adapter;
    private ListView list;
    private List<NativeLib.Data> items;
    private long data;
    private FusedLocationProviderClient locationClient;
    private ProgressBar throbber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        data = intent.getLongExtra("data", 0);
        System.out.println(data);
        throbber = findViewById(R.id.throbber);

        list = findViewById(R.id.trainList);
        items = new ArrayList<>();
        adapter = new DataAdapter(this, items);
        list.setAdapter(adapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        refresh();

        Button refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(v -> refresh() );

        Button delBtn = findViewById(R.id.delBtn);
        delBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sicher?")
                    .setMessage("Sollen wirklich alle Karten- und Fahrplandaten neu heruntergeladen werden?\nDas kann ein paar Minuten dauern.\nEs werden ca. 300MB an Daten verwendet.")
                    .setPositiveButton("Ja", (dialog, which) -> {
                        File binFile = new File(getFilesDir(), "data.bin");
                        binFile.delete();
                        finish();
                    })
                    .setNegativeButton("Nein", (dialog, which) -> {
                        dialog.dismiss();
                    }).show();
        });

    }

    private Handler handler;

    private void startUpdater(MainPage activity) {
        handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        handler.postDelayed(this, 5000);
                    }
                });
            }
        };

        handler.postDelayed(task, 5000);

    }

    @Override
    protected void onResume() {
        startUpdater(this);
        super.onResume();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationClient = LocationServices.getFusedLocationProviderClient(this);
            refresh();
        }
    }

    private void refresh() {
        adapter.clear();
        int now = LocalDateTime.now().toLocalTime().toSecondOfDay();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            items.add(new NativeLib.Data("Kein Standort!"));
            runOnUiThread(() -> adapter.notifyDataSetChanged());
            return;
        }
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            throbber.setVisibility(View.VISIBLE);
        });
        CurrentLocationRequest req = new CurrentLocationRequest.Builder().setDurationMillis(5000).setGranularity(Granularity.GRANULARITY_FINE).setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build();
        locationClient.getCurrentLocation(req, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(this, location -> {
            new Thread(() -> {
                try {
                    int jump = 0;
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        TextView loc = findViewById(R.id.loc);
                        NativeLib.Data[] trains = NativeLib.getData(this.data, lat, lon, -1);
                        runOnUiThread(() -> loc.setText(String.format("Location: %f, %f (%d trips)", lat, lon, trains.length)));
                        runOnUiThread(() -> adapter.clear());
                        if (trains.length == 0) {
                            runOnUiThread(() -> items.add(new NativeLib.Data("Keine Schiene")));
                        }
                        int i = 0;
                        for (NativeLib.Data train: trains) {
                            if (train.time > now && jump == 0) {
                                jump = i;
                            }
                            runOnUiThread(() -> items.add(train));
                            i += 1;
                        }
                    }else {
                        runOnUiThread(() -> items.add(new NativeLib.Data("Kein Standort")));
                    }
                    runOnUiThread(() -> throbber.setVisibility(View.INVISIBLE));
                    int j = jump;
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        list.setSelection(j);
                        if (j > 2)
                            list.smoothScrollToPosition(j-2);
                        else
                            list.smoothScrollToPosition(0);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        items.add(new NativeLib.Data(e.toString()));
                        adapter.notifyDataSetChanged();
                        throbber.setVisibility(View.INVISIBLE);
                    });
                    e.printStackTrace();
                }
            }).start();
        });
    }
}