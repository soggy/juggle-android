package gy.sog.Juggler;

import java.lang.Exception;
import java.net.*;
import java.util.Enumeration;

import android.app.Activity;
import android.app.Dialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;

import android.media.MediaPlayer;
import android.net.Uri;

import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.SurfaceView;

import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import gy.sog.Juggler.BluetoothServer;
import gy.sog.Juggler.JugglerService;

public class Juggler extends Activity implements SensorListener
{
    private final static String TAG = "Juggler";

    private ImageView ballImage;
    private SurfaceView sView;
    private Button btButton;
    private PopupWindow pw;
    private String server_address;
    private String server_port;
    private EditText server_address_input;
    private EditText server_port_input;
    public static final String PREFS_NAME = "JugglerSettings";
    protected Dialog mSplashDialog;
    protected Animation throwAnim, catchAnim;
    protected MediaPlayer mediaPlayer;

    private JugglerService jugglerService = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                jugglerService = ((JugglerService.JSBinder)service).getService();
                if (jugglerService != null) {
                    if (sView != null) {
                        sView.getHolder().addCallback(jugglerService);
                    } else {
                        Log.d(TAG, "onServiceConnected has jugglerService, but sView is null");
                    }
                } else {
                    Log.d(TAG, "onServiceConnected jugglerService is still null");
                }
            }
            public void onServiceDisconnected(ComponentName className) {
                jugglerService = null;
            }
        };


    public static boolean is_emulating() {
        return "sdk".equals(Build.PRODUCT);
    }

    public void btClient(View button) {
        if (! is_emulating()) {
            String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
            startActivityForResult(new Intent(enableBT), 0);
        }

        Intent intent = new Intent(this, BluetoothClient.class);
        startActivity(intent);
    }

    public void btServer(View button) {
        if (! is_emulating()) {
            String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
            startActivityForResult(new Intent(enableBT), 0);
        }

        Intent intent = new Intent(this, BluetoothServer.class);
        startActivity(intent);
    }

    public void doThrow(View button) {
	Log.d(TAG, "doThrow");
    	mediaPlayer = MediaPlayer.create(this, R.raw.throw_ball);
    	mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
            	mp.release();
            	mp = null;
            }
        });

	/*
	throwAnim.setAnimationListener(new Animation.AnimationListener() {
		public void onAnimationEnd(Animation anim) {
		    //ballImage.setAlpha(0);
		}
		public void onAnimationRepeat(Animation anim) {}
		public void onAnimationStart(Animation anim) {}
	    });
	*/

	ballImage.startAnimation(throwAnim);
    	mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }
    
    public void doCatch(View button) {
	Log.d(TAG, "doCatch");

	//Animation catchAnim = AnimationUtils.loadAnimation(this, R.anim.catch_anim);

    	mediaPlayer = MediaPlayer.create(this, R.raw.catch_ball1);
    	mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
            	mp.release();
            	mp = null;
            }
        });

	catchAnim.setAnimationListener(new Animation.AnimationListener() {
		public void onAnimationEnd(Animation anim) {
		    //ballImage.setAlpha(0xff);
		    mediaPlayer.start(); // no need to call prepare(); create() does that for you
		}
		public void onAnimationRepeat(Animation anim) {}
		public void onAnimationStart(Animation anim) {
		}
	    });

	ballImage.startAnimation(catchAnim);
    }

    private void registerSensorListeners() {
        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST);

        if (! accelSupported) {
            throw new Error("AARRGH (no accelerometer)");
        }

        boolean orientSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_FASTEST);

        //if (! orientSupported) {
            //throw new Error("AARRGH (no orientation sensor)");
        //}
    }

    private void unregisterSensorListeners() {
        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
	sensorMgr.unregisterListener(this);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MyStateSaver data = (MyStateSaver) getLastNonConfigurationInstance();
        if (data != null) {
            // Show splash screen if still loading
            if (data.showSplashScreen) {
                showSplashScreen();
            }
            setContentView(R.layout.main);        
     
            // Rebuild your UI with your saved state here
        } else {
            showSplashScreen();
            setContentView(R.layout.main);
            // Do your heavy loading here on a background thread
        }
        //setContentView(R.layout.main);
        
        //outView = (TextView)findViewById(R.id.output);
        //outView.setText(String.format("hello world... from CODE %f", 2.0f));
        
	ballImage = (ImageView)findViewById(R.id.jugglingball_view);
	throwAnim = AnimationUtils.loadAnimation(this, R.anim.throw_anim);
	catchAnim = AnimationUtils.loadAnimation(this, R.anim.catch_anim);

        //sView = (SurfaceView)findViewById(R.id.surface_view);
        if (sView == null) {
            Log.d(TAG, "ARRRG, no surface view");
        }

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        server_address = settings.getString("server_address", "192.168.1.113");
        server_port = settings.getString("server_port", "12345");
    }
    
    @Override
    protected void onStart() {
	Log.d(TAG, "onStart");

	//startService(new Intent(this, JugglerService.class));
        Intent bindIntent = new Intent(Juggler.this, JugglerService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);

	super.onStart();
	//registerSensorListeners();
    }

    @Override
    protected void onStop() {
	Log.d(TAG, "onStop");
	//stopService(new Intent(this, JugglerService.class));
        unbindService(serviceConnection);

	super.onStop();
	//unregisterSensorListeners();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("server_address", server_address);
      editor.putString("server_port", server_port);

      // Commit the edits!
      editor.commit();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        MyStateSaver data = new MyStateSaver();
        // Save your important data here
     
        if (mSplashDialog != null) {
            data.showSplashScreen = true;
            removeSplashScreen();
        }
        return data;
    }
    
     
    /**
     * Removes the Dialog that displays the splash screen
     */
    protected void removeSplashScreen() {
        if (mSplashDialog != null) {
            mSplashDialog.dismiss();
            mSplashDialog = null;
        }
    }
     
    /**
     * Shows the splash screen over the full Activity
     */
    protected void showSplashScreen() {
        mSplashDialog = new Dialog(this, R.style.SplashScreen);
        mSplashDialog.setContentView(R.layout.splashscreen);
        mSplashDialog.setCancelable(false);
        mSplashDialog.show();
     
        // Set Runnable to remove splash screen just in case
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          //@Override
          public void run() {
            removeSplashScreen();
          }
        }, 3000);
    }
    
    /**
     * Simple class for storing important data across config changes
     */
    private class MyStateSaver {
        public boolean showSplashScreen = false;
        // Your other important fields here
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void onAccuracyChanged(int sensor, int accuracy) {
        // outView.setText(String.format("onAccuracyChanged: sensor: %d, accuracy: %d", sensor, accuracy));
    }

  
    private float[] last_orientation_data = null;

    public void onSensorChanged(int sensor, float[] values) {
        if (last_orientation_data == null) {
            last_orientation_data = new float[3];
            last_orientation_data[0] = 0.0f;
            last_orientation_data[1] = 0.0f;
            last_orientation_data[2] = 0.0f;
        }

        switch (sensor) {
        case SensorManager.SENSOR_ACCELEROMETER: {
            // All values are in SI units (m/s^2) and measure contact forces.
            // values[0]: force applied by the device on the x-axis
            // values[1]: force applied by the device on the y-axis
            // values[2]: force applied by the device on the z-axis
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
                //outView.setText(data);
                sendAccelData(server_address, (Integer.parseInt(server_port)), data);
            } catch (JSONException e) {
                Log.d("sendAccelData", "JSONException: " + e);
            }
            break;
        }
        case SensorManager.SENSOR_ORIENTATION: {
            // All values are angles in degrees.
            // values[0]: Azimuth, rotation around the Z axis (0<=azimuth<360). 0 = North, 90 = East, 180 = South, 270 = West
            // values[1]: Pitch, rotation around X axis (-180<=pitch<=180), with positive values when the z-axis moves toward the y-axis.
            // values[2]: Roll, rotation around Y axis (-90<=roll<=90), with positive values when the z-axis moves toward the x-axis.
            last_orientation_data[0] = values[0];
            last_orientation_data[1] = values[1];
            last_orientation_data[2] = values[2];
            break;
        }
        default: break;
        }
    }


    public void sendAccelData(String server, int port, String msgStr) {
    try {
        DatagramSocket s = new DatagramSocket();
        InetAddress saddr = InetAddress.getByName(server);
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
    
    //Called when "enter server IP address" button is clicked
    public void findServer(View button){
        setContentView(R.layout.server_input);
        //Server IP
        server_address_input=(EditText) findViewById(R.id.server_ip_text);
        server_address_input.setText(server_address);
        
        //Server port
        server_port_input=(EditText) findViewById(R.id.server_port_text);
        server_port_input.setText(server_port);
        
        TextView my_ip=(TextView)findViewById(R.id.my_ip_address);
        my_ip.setText(getLocalIpAddress());
        
    }
    
    public void serverConnect(View button){

        server_address=server_address_input.getText().toString();
        server_port=server_port_input.getText().toString();
        server_address_input.setInputType(0);
        server_port_input.setInputType(0);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(server_address_input.getWindowToken(), 0);
        setContentView(R.layout.main);
        //outView = (TextView)findViewById(R.id.output);
    }
    
    public void serverBack(View button){    

    	//MEDIAPLAYER TEST CODE HERE
    	
    	//mediaPlayer.reset();
    	mediaPlayer = MediaPlayer.create(this, R.raw.kitten2);
    	mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
            	mp.release();
            	mp = null;
            }
        });
    	mediaPlayer.start(); // no need to call prepare(); create() does that for you

    	
    	//END TEST CODE
        server_address_input.setInputType(0);
        server_port_input.setInputType(0);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(server_address_input.getWindowToken(), 0);
        setContentView(R.layout.main);
        //outView = (TextView)findViewById(R.id.output);
    }
    
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e(LOG_TAG, ex.toString());
        	//Do nothing
        }
        return null;
    }
    
    public void pickSound(View button){
        Intent intent = new Intent(this, SoundFXChooser.class);
        startActivity(intent);
    }    
}
