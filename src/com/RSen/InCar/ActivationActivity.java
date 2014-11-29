package com.RSen.InCar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

public class ActivationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activation);
		manualActivate();
		finish();
	}

	private void manualActivate() {
		Intent intent = new Intent(getApplicationContext(), MyService.class);

		if (!MyService.isRunning) {
			startService(intent);
		}

		// wait for initialized
		final Handler handler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				Intent broadcast = new Intent("manual-activation");
				LocalBroadcastManager.getInstance(getApplicationContext())
						.sendBroadcast(broadcast);
				return true;
			}
		});
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (!MyService.isRunning) {

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {

					}

				}
				handler.sendEmptyMessage(0);
			}
		};
		new Thread(runnable).start();

	}
}
