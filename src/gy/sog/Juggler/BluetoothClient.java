package gy.sog.Juggler;

import gy.sog.Juggler.BluetoothShared;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.Set;
import java.io.IOException;



public class BluetoothClient extends Activity {

	private BluetoothAdapter b;

        private BluetoothSocket bts = null;
	
	@Override
	public void onStart() {
                super.onStart();
                setContentView(R.layout.juggle);

                final TextView outView = (TextView)findViewById(R.id.juggle_output);

		b = BluetoothAdapter.getDefaultAdapter();

                if (b != null) {
                    final Set<BluetoothDevice> paired = b.getBondedDevices();

                    BroadcastReceiver dm = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String r = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                            BluetoothDevice remote;
                            remote = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                            if (paired.contains(remote)) {
                                try {
                                    bts = remote.createRfcommSocketToServiceRecord(BluetoothShared.juggleUUID);
                                } catch (IOException e) {
                                    // no problem.
                                }
                            } else {
                                outView.setText(String.format("onReceive: %s", r));
                            }
                        }
                    };

                    registerReceiver(dm, new IntentFilter(BluetoothDevice.ACTION_FOUND));

                    if (! b.isDiscovering()) {
                        outView.setText("scanning....");
                        b.startDiscovery();
                    }
                } else {
                    //setContentView(R.layout.device_list);
                    outView.setText("no bluetooth...");
                }
	}

        @Override
        public void onStop() {
                super.onStop();
        }
}
