package gy.sog.Juggler;

import java.util.concurrent.locks.ReentrantLock;

import android.app.Service;

import android.content.Intent;

import android.hardware.SensorListener;
import android.hardware.SensorManager;

import android.os.Binder;
import android.os.IBinder;

import android.util.Log;

import android.view.SurfaceHolder;


public class JugglerService extends Service 
    implements SensorListener, SurfaceHolder.Callback
{
    private final static String TAG = "JugglerService";
    private Hand hand = new Hand(200);

    private ReentrantLock surfaceHolderLock = new ReentrantLock();
    private SurfaceHolder surfaceHolder = null;

    @Override
    public void onCreate() {
	Log.d(TAG, "onCreate");
        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorMgr == null) {
            Log.w(TAG, "onCreate got null SensorManager");
            return;
        }

        boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST);
        
        if (! accelSupported) {
            throw new Error("AARRGH (no accelerometer)");
        }
        
        boolean orientSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_FASTEST);        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.d(TAG, String.format("onStartCommand intent:%s flags:%d startId:%d", intent.toString(), flags, startId));

	return Service.START_STICKY;
    }

    public class JSBinder extends Binder {
        JugglerService getService() {
            return JugglerService.this;
        }
    }
    private final IBinder binder = new JSBinder();

    @Override
    public IBinder onBind(Intent intent) {
	Log.d(TAG, String.format("onBind intent:%s", intent.toString()));
	return binder;
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

        if (surfaceHolder != null) { // avoid locking every time if null, even though...
            surfaceHolderLock.lock();
            try {
                if (surfaceHolder != null) { // ... we still have to check once the lock is aquired
                    //Log.d(TAG, String.format("onSensorChanged rendering %d %s to %s", sensor, values.toString(), surfaceHolder.toString()));
                }
            } finally {
                surfaceHolderLock.unlock();
            }
        }

        int action = hand.detectAction();
        if (action > 0) {
            Log.d(TAG, String.format("onSensorChanged hand returned action %d", action));
        }
    }


    /// SurfaceHolder.Callback interface
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // This is called immediately after any structural changes (format or size) have been made to the surface.
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // This is called immediately after the surface is first created.
        surfaceHolderLock.lock();
        try {
            Log.d(TAG, String.format("setting surfaceHolder to %s", holder.toString()));
            surfaceHolder = holder;
        } finally {
            surfaceHolderLock.unlock();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // This is called immediately before a surface is being destroyed.
        surfaceHolderLock.lock();
        try {
            Log.d(TAG, "setting surfaceHolder to null");
            surfaceHolder = null;
        } finally {
            surfaceHolderLock.unlock();
        }
    }

}
