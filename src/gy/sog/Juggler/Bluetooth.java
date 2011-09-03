package gy.sog.Juggler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class Bluetooth extends Activity{

	private BluetoothAdapter b;
	static final int DISCOVERY_REQUEST = 1;
	
	public void startServer(){	
	b=BluetoothAdapter.getDefaultAdapter();
	String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
	startActivityForResult(new Intent(aDiscoverable), DISCOVERY_REQUEST);
	}
}
