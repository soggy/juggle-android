package gy.sog.Juggler;

import android.util.Log;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.hardware.SensorListener;
import android.hardware.SensorManager;


public class JugglerService extends Service implements SensorListener
{
    private final static String TAG = "JugglerService";

    @Override
    public void onCreate() {
	Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.d(TAG, String.format("onStartCommand intent:%s flags:%d startId:%d", intent.toString(), flags, startId));
	return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
	Log.d(TAG, String.format("onBind intent:%s", intent.toString()));
	return null;
    }

    /// SensorListener interface
    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {
	Log.d(TAG, String.format("onAccuracyChanged sensor:%d accuracy:%d", sensor, accuracy));
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
	Log.d(TAG, String.format("onSensorChanged sensor:%d values:%s", sensor, values.toString()));
    }
}
