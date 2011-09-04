package gy.sog.Juggler;

import gy.sog.Juggler.BluetoothShared;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.TextView;

public class BluetoothServer extends Activity {

	private BluetoothAdapter b;
	static final int DISCOVERY_REQUEST = 1;

        private TextView outView;
	
	@Override
	public void onStart() {
		super.onStart();
                setContentView(R.layout.juggle);

                outView = (TextView)findViewById(R.id.juggle_output);

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
				soc = b.listenUsingRfcommWithServiceRecord("juggler", BluetoothShared.juggleUUID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				BluetoothSocket socket = soc.accept();
                                outView.setText("received a bluetooth connection");

                                InputStream s = socket.getInputStream();
                                byte[] buffer = new byte[1024];
                                int r = s.read(buffer);
                                String message = new String(buffer, 0, r);
                                outView.setText(String.format("message: %s", message));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
		}
	}
	
}
