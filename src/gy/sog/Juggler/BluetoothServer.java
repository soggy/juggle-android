package gy.sog.Juggler;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class BluetoothServer extends Activity{

	private BluetoothAdapter b;
	static final int DISCOVERY_REQUEST = 1;
	
	@Override
	public void onStart(){
		b=BluetoothAdapter.getDefaultAdapter();
		String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
		startActivityForResult(new Intent(aDiscoverable), DISCOVERY_REQUEST);
	}
	
}
