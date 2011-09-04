package gy.sog.Juggler;

import java.lang.Exception;
import java.net.*;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import gy.sog.Juggler.BluetoothServer;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;


import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

public class Juggler extends Activity implements SensorListener
{
    private TextView outView;
    private Button btButton;
    private PopupWindow pw;
    private String server_address;
    private String server_port;
    private EditText server_address_input;
    private EditText server_port_input;
    public static final String PREFS_NAME = "JugglerSettings";
    private Dialog mSplashDialog;

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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        showSplashScreen();
        setContentView(R.layout.main);
        
        outView = (TextView)findViewById(R.id.output);

        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_UI);

        if (! accelSupported) {
            throw new Error("AARRGH");
        }

        outView.setText(String.format("hello world... from CODE %f", 2.0f));
        
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        server_address = settings.getString("server_address", "192.168.1.113");
        server_port = settings.getString("server_port", "Enter Port...");
    }
    
     
    /**
     * Removes the Dialog that displays the splash screen
     */
    private void removeSplashScreen() {
        if (mSplashDialog != null) {
            mSplashDialog.dismiss();
            mSplashDialog = null;
        }
    }
     
    /**
     * Shows the splash screen over the full Activity
     */
    private void showSplashScreen() {
    	mSplashDialog = new Dialog(this);
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
    
    

    public void onAccuracyChanged(int sensor, int accuracy) {
        outView.setText(String.format("onAccuracyChanged: sensor: %d, accuracy: %d", sensor, accuracy));
    }

  
    public void onSensorChanged(int sensor, float[] values) {
        // All values are in SI units (m/s^2) and measure contact forces.
        // values[0]: force applied by the device on the x-axis
        // values[1]: force applied by the device on the y-axis
        // values[2]: force applied by the device on the z-axis
        if (sensor != SensorManager.SENSOR_ACCELEROMETER) {
            return;
        }
        try {
	       JSONObject jEvent = new JSONObject();
	       JSONObject jData = new JSONObject();
	       jData.put("hand", "right");
	       jData.put("x", values[0]);
	       jData.put("y", values[1]);
	       jData.put("z", values[2]);
           jEvent.put("type", "sensor_data");
           jEvent.put("data", jData);
		
	   
	       String data = String.format("onSensorChanged: sensor: %d, [x,y,z]=[%f,%f,%f]\n", sensor, values[0], values[1], values[2]);
           outView.setText(data);
	       sendAccelData(server_address, 12345, jEvent.toString());
		} catch (JSONException e) {
	        Log.d("sendAccelData", "JSONException: " + e);
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
    	
    }
    
    public void serverConnect(View button){

    	server_address=server_address_input.getText().toString();
    	server_port=server_port_input.getText().toString();
    	server_address_input.setInputType(0);
    	server_port_input.setInputType(0);
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(server_address_input.getWindowToken(), 0);
    	setContentView(R.layout.main);
    }
    
    public void serverBack(View button){	
    	//Bar
    	server_address_input.setInputType(0);
    	server_port_input.setInputType(0);
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(server_address_input.getWindowToken(), 0);
    	setContentView(R.layout.main);
    }
    
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("server_address", server_address);
      editor.putString("server_port", server_port);

      // Commit the edits!
      editor.commit();
    }
    
}
