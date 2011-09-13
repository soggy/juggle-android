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

	Context context = getApplicationContext();
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	prefs.registerOnSharedPreferenceChangeListener(this);
	loadSharedPrefs(prefs);
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

	Context context = getApplicationContext();
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /// SensorListener interface
    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {
	//Log.d(TAG, String.format("onAccuracyChanged sensor:%d accuracy:%d", sensor, accuracy));
    }

    private float[] last_orientation_data = null;

    @Override
    public void onSensorChanged(int sensor, float[] values) {
	//Log.d(TAG, String.format("onSensorChanged sensor:%d values:%s", sensor, values.toString()));

	// FIX: need to get message from hand?
        if (last_orientation_data == null) {
            last_orientation_data = new float[3];
            last_orientation_data[0] = 0.0f;
            last_orientation_data[1] = 0.0f;
            last_orientation_data[2] = 0.0f;
        }
	if (sensor == SensorManager.SENSOR_ORIENTATION) {
            // All values are angles in degrees.
            // values[0]: Azimuth, rotation around the Z axis (0<=azimuth<360). 0 = North, 90 = East, 180 = South, 270 = West
            // values[1]: Pitch, rotation around X axis (-180<=pitch<=180), with positive values when the z-axis moves toward the y-axis.
            // values[2]: Roll, rotation around Y axis (-90<=roll<=90), with positive values when the z-axis moves toward the x-axis.
            last_orientation_data[0] = values[0];
            last_orientation_data[1] = values[1];
            last_orientation_data[2] = values[2];
	}

        hand.updateSensorData(sensor, 0.0f, values);
	if (sendSensorData && sensor == SensorManager.SENSOR_ACCELEROMETER)
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
	loadSharedPrefs(prefs);
    }


    //
    // Non-Interface implementation code:
    //

    protected void loadSharedPrefs(SharedPreferences prefs) {
	Log.d(TAG, String.format("loadSharedPrefs %s", prefs.toString()));
	sendSensorData = prefs.getBoolean(JugglerPreferenceActivity.PREF_SERVER_SEND_DATA, false);
	serverAddr = prefs.getString(JugglerPreferenceActivity.PREF_SERVER_IP, "localhost");
	serverPort = Integer.parseInt(prefs.getString(JugglerPreferenceActivity.PREF_SERVER_PORT, "12345"));
	Log.d(TAG, String.format("loadSharedPrefs done with sendSensorData:%s serverAddr:%s serverPort:%d",
				 sendSensorData, serverAddr, serverPort));
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
	    InetAddress saddr = InetAddress.getByName(serverAddr);
	    int port = serverPort;
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
	    jData.put("azimuth", last_orientation_data[0]);
	    jData.put("pitch", last_orientation_data[1]);
	    jData.put("roll", last_orientation_data[2]);
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
