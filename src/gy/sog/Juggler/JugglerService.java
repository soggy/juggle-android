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
    private Hand hand = new Hand(200);

    @Override
    public void onCreate() {
	Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.d(TAG, String.format("onStartCommand intent:%s flags:%d startId:%d", intent.toString(), flags, startId));

        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorMgr == null) {
            Log.w(TAG, "onStartCommand got null SensorManager");
            return Service.START_STICKY;
        }

        boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST);
        
        if (! accelSupported) {
            throw new Error("AARRGH (no accelerometer)");
        }
        
        boolean orientSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_FASTEST);
        
	return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
	Log.d(TAG, String.format("onBind intent:%s", intent.toString()));
	return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
	sensorMgr.unregisterListener(this);
    }

    /// SensorListener interface
    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {
	//Log.d(TAG, String.format("onAccuracyChanged sensor:%d accuracy:%d", sensor, accuracy));
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
	//Log.d(TAG, String.format("onSensorChanged sensor:%d values:%s", sensor, values.toString()));
        hand.updateSensorData(sensor, 0.0f, values);
        int action = hand.detectAction();
        if (action > 0) {
            Log.d(TAG, String.format("onSensorChanged hand returned action %d", action));
        }
    }
}
