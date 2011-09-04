package gy.sog.Juggler;

import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

public class BluetoothServer extends Activity {

	private BluetoothAdapter b;
	static final int DISCOVERY_REQUEST = 1;
	
	@Override
	public void onStart(){
		super.onStart();
                setContentView(R.layout.juggle);

		b = BluetoothAdapter.getDefaultAdapter();
                if (b != null) {
		    String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
		    startActivityForResult(new Intent(aDiscoverable), DISCOVERY_REQUEST);
                }
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == DISCOVERY_REQUEST && resultCode > 0){
			BluetoothServerSocket soc = null;
			try {
				soc = b.listenUsingRfcommWithServiceRecord("juggler", UUID.randomUUID());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				BluetoothSocket socket = soc.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
		}
	}
	
}
