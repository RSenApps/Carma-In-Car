package com.RSen.InCar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class BluetoothStartServiceReceiver extends BroadcastReceiver {
	public BluetoothStartServiceReceiver() {

	}

	public BluetoothStartServiceReceiver(Context context) {
		IntentFilter filter = new IntentFilter();
		filter.addAction("service-started");
		filter.addAction("start-on-bluetooth");
		filter.addAction("service-stopped");
		LocalBroadcastManager.getInstance(context).registerReceiver(this,
				filter);
		Intent i = new Intent(context, BluetoothStartService.class);
		context.startService(i);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, BluetoothStartService.class);
		if (intent.getAction().matches("service-started")) {
			context.stopService(i);
		} else {
			context.startService(i);
		}
	}
}
