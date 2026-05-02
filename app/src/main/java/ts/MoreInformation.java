package ts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ts.jni.NativeLib;

public class MoreInformation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_more_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        NativeLib.Data data = (NativeLib.Data) intent.getSerializableExtra("data");
        if (data == null) {
            finish();
            return;
        }

        TextView routeName = findViewById(R.id.routeName);
        TextView headsign = findViewById(R.id.more_info_headsign);
        TextView dep = findViewById(R.id.prevDep);
        TextView arr = findViewById(R.id.nextArr);
        TextView time = findViewById(R.id.time_diff);
        TextView distance = findViewById(R.id.distance);
        TextView speed = findViewById(R.id.speed);

        routeName.setText(data.routeName);
        headsign.setText(data.headsign);
        dep.setText(String.format("Abfahrt in %s um %s Uhr", data.prevStop, NativeLib.Data.format(data.previousDeparture)));
        arr.setText(String.format("Ankunft in %s um %s Uhr", data.nextStop, NativeLib.Data.format(data.nextArrival)));

        time.setText(String.format("Benötigte Zeit: %.2f Minuten", (double)data.timeDiff/60.0));
        distance.setText(String.format("Zurückgelegte Strecke: %.2f km", data.distance));
        speed.setText(String.format("Durchschnittsgeschwindigkeit: %.2f km/h",  data.distance / ((double)data.timeDiff/60.0/60.0)));

    }
}