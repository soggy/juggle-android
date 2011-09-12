package gy.sog.Juggler;

import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.hardware.SensorListener;
import android.hardware.SensorManager;

import android.os.Binder;
import android.os.IBinder;

import android.preference.PreferenceManager;

import android.util.Log;

import android.view.SurfaceHolder;


public class JugglerService extends Service 
    implements SensorListener, SurfaceHolder.Callback, 
	       SharedPreferences.OnSharedPreferenceChangeListener
{
    private final static String TAG = "JugglerService";
    private Hand hand = new Hand(200);

    private ReentrantLock surfaceHolderLock = new ReentrantLock();
    private SurfaceHolder surfaceHolder = null;

    // set from shared prefs
    private boolean sendSensorData = false;
    private String serverAddr = "";
    private int serverPort = 0;

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

	loadSharedPrefs();
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
	sendAccelData(sensor, values);

        if (surfaceHolder != null) { // avoid locking every time if null, even though...
            surfaceHolderLock.lock();
            try {
                if (surfaceHolder != null) { // ... we still have to check once the lock is aquired
                    //Log.d(TAG, String.format("onSensorChanged rendering %d %s to %s", sensor, values.toString(), surfaceHolder.toString()));
		    //FIX: need to actually draw on something here (meaning, need to make the Activity contain a SurfaceHolder which gets the ball+sensor data rendered to it.
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


    //
    // OnSharedPreferenceChangeListener interface impl
    //
    @Override 
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	Log.d(TAG, String.format("onSharedPreferenceChanged for key %s", key));
    }


    //
    // Non-Interface implementation code:
    //

    protected void loadSharedPrefs() {
	Context context = getApplicationContext();
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	Log.d(TAG, String.format("loadSharedPrefs found %s", prefs.toString()));
	sendSensorData = prefs.getBoolean(JugglerPreferenceActivity.PREF_SERVER_SEND_DATA, false);
	serverAddr = prefs.getString(JugglerPreferenceActivity.PREF_SERVER_IP, "localhost");
	serverPort = Integer.parseInt(prefs.getString(JugglerPreferenceActivity.PREF_SERVER_PORT, "12345"));
    }

    protected void sendAccelData(int sensor, float[] values) {
	// String server, int port, String msgStr) {
	String msgStr = formatSensorMessage(sensor, values);
	if (msgStr == null) {
	    Log.d(TAG, String.format("sendAccelData failed to format the sensor message sensor:%d values:%s", 
				     sensor, values.toString()));
	    return;
	}

	try {
	    DatagramSocket s = new DatagramSocket();
	    InetAddress saddr = InetAddress.getByName("192.168.1.10"); //server);
	    int port = 12345;
	    int msg_length=msgStr.length();
	    byte[] message = msgStr.getBytes();
	    DatagramPacket p = new DatagramPacket(message, msg_length,saddr,port);
	    s.send(p);
	} catch (SocketException e) {
	    Log.d("sendAccelData", "SocketException: " + e);
	} catch (UnknownHostException e) {
	    Log.d("sendAccelData", "HostException: " + e);
	} catch (java.io.IOException e) {
	    Log.d("sendAccelData", "IOException: " + e);
	}
    }

    protected String formatSensorMessage(int sensor, float[] values) {
	try {
	    JSONObject jEvent = new JSONObject();
	    JSONObject jData = new JSONObject();
	    jData.put("hand", "right");
	    jData.put("x", values[0]);
	    jData.put("y", values[1]);
	    jData.put("z", values[2]);
	    jData.put("azimuth", 0.0); //last_orientation_data[0]);
	    jData.put("pitch", 0.0); //last_orientation_data[1]);
	    jData.put("roll", 0.0); //last_orientation_data[2]);
	    jEvent.put("type", "sensor_data");
	    jEvent.put("data", jData);
	    
	    String data = jEvent.toString();
	    return data;
	} catch (JSONException e) {
	    Log.d(TAG, "formatSensorMessage JSONException: " + e);
	}
	return null;
    }

}
