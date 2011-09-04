package gy.sog.Juggler;

import java.lang.Exception;
import java.net.*;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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

public class Juggler extends Activity implements SensorListener, View.OnClickListener
{
    private TextView outView;
    private Button btButton;
    private boolean btStarted;
    private PopupWindow pw;

    @Override
    public void onClick(View v) {
	if (btStarted) {
	    Log.d("Juggler", "already started bluetooth, ignoring click");
	    return;
	}

	// Perform action on click

        String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        startActivityForResult(new Intent(enableBT), 0);

	Intent intent = new Intent(this, BluetoothServer.class);
	startActivity(intent);

	// old style attempt?
	// int DISCOVERY_REQUEST = 1;
	
	// BluetoothAdapter b;
	// b=BluetoothAdapter.getDefaultAdapter();
	// String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
	// startActivityForResult(new Intent(aDiscoverable), DISCOVERY_REQUEST);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button left_button = (Button) findViewById(R.id.left_hand);
        Button right_button = (Button) findViewById(R.id.right_hand);
        
        left_button.setEnabled(false);
        right_button.setEnabled(false);

	btStarted = false;

        outView = (TextView) findViewById(R.id.output);

	// only start bluetooth stuff if you click the button
	btButton = (Button) findViewById(R.id.btbutton);
	btButton.setOnClickListener(this);

        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_UI);

        if (! accelSupported) {
            throw new Error("AARRGH");
        }

        outView.setText(String.format("hello world... from CODE %f", 2.0f));
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
	       sendAccelData("192.168.1.129", 12345, jEvent.toString());
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
    
    public void identifyPair(View button){
    	//Add code for initializing bluetooth activity here
    	
    }
    
    public void findServer(View button){
    	
    	Display display = getWindowManager().getDefaultDisplay(); 
    	int display_width = display.getWidth();
    	int display_height = display.getHeight();
    	//this is the code for popup window

    	LayoutInflater inflater = (LayoutInflater) Juggler.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	//Here x is the name of the xml which contains the popup components
    	pw = new PopupWindow(inflater.inflate(R.layout.server_input,null, false),display_width,display_height,true);
    	//Here y is the id of the root component

    	//pw.showAtLocation(findViewById(R.id.main), Gravity.CENTER, 0,0);
    	pw.showAsDropDown(findViewById(R.id.find_server));
    	
    }
    
//    EditText serverTextBox = (EditText) findViewById(R.id.server_ip_text);
//    serverTextBox.setOnClickListener(new View.OnClickListener() {
//        public void onClick(View v) {
//            //Foo
//        }
//
//    }
    
    public void serverConnect(View button){
    	//Foo
    }
    
    public void serverBack(View button){	
    	//Bar
    	pw.dismiss();
    }
    
}
