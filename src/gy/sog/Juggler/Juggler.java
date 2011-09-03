package gy.sog.Juggler;

import java.lang.Exception;
import java.net.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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

	String data = String.format("onSensorChanged: sensor: %d, [x,y,z]=[%f,%f,%f]\n", sensor, values[0], values[1], values[2]);
        outView.setText(data);
	sendAccelData("192.168.1.113", 12345, data);
    }

    public void sendAccelData(String server, int port, String msgStr) {
	try {
	    DatagramSocket s = new DatagramSocket();
	    InetAddress local = InetAddress.getByName(server);
	    int msg_length=msgStr.length();
	    byte[] message = msgStr.getBytes();
	    DatagramPacket p = new DatagramPacket(message, msg_length,local,port);
	    s.send(p);
	} catch (SocketException e) {
	    // arg!
	} catch (UnknownHostException e) {
	} catch (java.io.IOException e) {
	}
    }
}
