package gy.sog.Juggler;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;

public class BluetoothClient extends Activity {

	private BluetoothAdapter b;
	
	@Override
	public void onStart() {
                super.onStart();

                Log.d("bluetooth client start", "start");

		b = BluetoothAdapter.getDefaultAdapter();

                BroadcastReceiver dm = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String r = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                        BluetoothDevice remote;
                        remote = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        Log.d("onReceive", r);
                    }
                };

                registerReceiver(dm, new IntentFilter(BluetoothDevice.ACTION_FOUND));

                if (! b.isDiscovering()) {
                    b.startDiscovery();
                }

                Log.d("bluetooth client start", "exit");
	}

        @Override
        public void onStop() {
                super.onStop();
        }
}
