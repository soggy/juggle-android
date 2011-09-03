package gy.sog.Juggler;

import java.lang.Exception;

import android.app.Activity;
import android.os.Bundle;

import android.hardware.SensorListener;
import android.hardware.SensorManager;

import android.widget.TextView;

public class Juggler extends Activity implements SensorListener
{
    private TextView outView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        outView = (TextView) findViewById(R.id.output);

        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_UI);

        if (! accelSupported) {
            throw new Error("AARRGH");
        }

        outView.setText("hello world... from CODE");
    }

    public void onAccuracyChanged(int sensor, int accuracy) {
        outView.setText(String.format("%d", sensor));
    }

  
    public void onSensorChanged(int sensor, float[] values) {
        outView.setText(String.format("%d", sensor));
    }
}
