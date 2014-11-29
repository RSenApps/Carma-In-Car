package com.RSen.InCar;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class BluetoothStartService extends Service implements
		BluetoothHelperListener {
	private boolean started = false;
	private static BluetoothHelper helper;

	public BluetoothStartService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean("start_on_bluetooth", true)) {
			stopSelf();
		} else if (!started) {
			startListening();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void startListening() {
		helper = new BluetoothHelper(getApplicationContext(), this);
		helper.start();
	}

	private void stopListening() {
		if (helper != null) {
			helper.stop();
		}
	}

	@Override
	public void onDestroy() {
		stopListening();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void startUsingBluetoothHeadset() {

	}

	@Override
	public void stopUsingBluetoothHeadset() {

	}

	@Override
	public void headsetDisconnected() {
	}

	@Override
	public void headsetConnected() {
		Intent intent = new Intent(getApplicationContext(), MyService.class);
		if (!MyService.isRunning) {
			startService(intent);
		}

	}
}
