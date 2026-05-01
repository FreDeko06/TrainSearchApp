package ts;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;

import ts.jni.NativeLib;

public class DataAdapter extends ArrayAdapter<NativeLib.Data> {
    public DataAdapter(@NonNull Context context, @NonNull List<NativeLib.Data> objects) {
        super(context, 0, objects);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.train_list_item, parent, false);
        }

        NativeLib.Data data = getItem(position);

        TextView route = convertView.findViewById(R.id.route);
        TextView time = convertView.findViewById(R.id.time);
        TextView timeLeft = convertView.findViewById(R.id.timeLeft);
        TextView next = convertView.findViewById(R.id.next);
        TextView headsign = convertView.findViewById(R.id.headsign);
        TextView delay = convertView.findViewById(R.id.delay);


        route.setText(data.routeName);
        time.setText(format(data.time - data.delay));
        headsign.setText(data.headsign);

        String delStr = String.format("%.1f", Math.abs((float) data.delay / 60.0));
        if (data.delay >= 0) {
            delStr = "+ " + delStr;
        }else {
            delStr = "- " + delStr;
        }

        delay.setText(delStr);


        int delta = LocalDateTime.now().toLocalTime().toSecondOfDay() - data.time;
        String deltaTime = format(Math.abs(delta));
        if (delta < 0) {
            deltaTime = "- " + deltaTime;
            convertView.setBackgroundColor(Color.argb(100, 255, 255, 0));
        }else {
            deltaTime = "+ " + deltaTime;
            convertView.setBackgroundColor(Color.argb(100, 255, 0, 0));
        }
        if (Math.abs(delta) < 5 * 60) {
            convertView.setBackgroundColor(Color.argb(100, 0, 255, 0));
        }

        timeLeft.setText(deltaTime);
        next.setText(data.nextStop);
        return convertView;
    }
    private String format(int time) {
        int seconds = time % 60;
        int minutes = ((time - seconds) / 60) % 60;
        int hours = (time - seconds - minutes * 60) / 60 / 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
