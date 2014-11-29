package com.RSen.InCar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	ImageButton toggleService;
	TextView toggleServiceLabel;

	LinearLayout listenLayout;
	ImageButton listen;

	LinearLayout hotwordLayout;
	ImageButton hotwordToggle;
	TextView hotwordToggleLabel;

	LinearLayout waveLayout;
	ImageButton waveToggle;
	TextView waveToggleLabel;
	private BroadcastReceiver manualActivationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			serviceStartedLayout();
		}
	};
	private BroadcastReceiver serviceStoppedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			serviceStoppedLayout();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("newUser", true)) {
			prefs.edit().putBoolean("newUser", false).commit();
			Intent i = new Intent(this, TutorialActivity.class);
			startActivity(i);
		}
		setupUI();
		new BluetoothStartServiceReceiver(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				serviceStoppedReceiver, new IntentFilter("service-stopped"));
		LocalBroadcastManager.getInstance(this).registerReceiver(
				manualActivationReceiver, new IntentFilter("service-started"));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.settings) {

			if (MyService.isRunning) {
				final Intent myServiceIntent = new Intent(
						getApplicationContext(), MyService.class);
				stopService(myServiceIntent);
				serviceStoppedLayout();
			}
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		} else if (item.getItemId() == R.id.help) {
			Intent i = new Intent(this, TutorialActivity.class);
			startActivity(i);
		} else if (item.getItemId() == R.id.feedback) {
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			String aEmailList[] = { "RSenApps+Carma@gmail.com" };
			emailIntent
					.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Carma Feedback/Bug Report");
			emailIntent.setType("plain/text");
			startActivity(emailIntent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupUI() {
		toggleService = (ImageButton) findViewById(R.id.serviceToggle);
		toggleServiceLabel = (TextView) findViewById(R.id.serviceToggleLabel);

		listenLayout = (LinearLayout) findViewById(R.id.listenLayout);
		listen = (ImageButton) findViewById(R.id.listenButton);

		hotwordLayout = (LinearLayout) findViewById(R.id.hotwordLayout);
		hotwordToggle = (ImageButton) findViewById(R.id.hotwordToggle);
		hotwordToggleLabel = (TextView) findViewById(R.id.hotwordToggleLabel);

		waveLayout = (LinearLayout) findViewById(R.id.waveLayout);
		waveToggle = (ImageButton) findViewById(R.id.waveToggle);
		waveToggleLabel = (TextView) findViewById(R.id.waveToggleLabel);

		if (MyService.isRunning) {
			serviceStartedLayout();
		} else {
			serviceStoppedLayout();
		}
		setupOnClickListeners();

	}

	private void serviceStartedLayout() {
		toggleService.setImageResource(R.drawable.ic_stop_service);
		toggleServiceLabel.setText("Stop");
		listenLayout.setVisibility(View.VISIBLE);
		hotwordLayout.setVisibility(View.GONE);
		waveLayout.setVisibility(View.GONE);
	}

	private void serviceStoppedLayout() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("listenHotword", true))// default layout is true
		{
			hotwordToggle.setImageResource(R.drawable.ic_listen);
			hotwordToggleLabel.setText("Hotword On");
		} else {
			hotwordToggle.setImageResource(R.drawable.ic_hotword);
			hotwordToggleLabel.setText("Hotword Off");
		}
		if (prefs.getBoolean("wave", true))// default layout is true
		{
			waveToggle.setImageResource(R.drawable.ic_wave_green);
			waveToggleLabel.setText("Hand Wave On");
		} else {
			waveToggle.setImageResource(R.drawable.ic_wave);
			waveToggleLabel.setText("Hand Wave Off");
		}
		toggleService.setImageResource(R.drawable.ic_start_service);
		toggleServiceLabel.setText("Start");
		listenLayout.setVisibility(View.GONE);
		hotwordLayout.setVisibility(View.VISIBLE);
		waveLayout.setVisibility(View.VISIBLE);
	}

	private void setupOnClickListeners() {
		toggleService.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final Intent myServiceIntent = new Intent(
						getApplicationContext(), MyService.class);
				if (MyService.isRunning) {
					stopService(myServiceIntent);
					serviceStoppedLayout();
				} else {
					startService(myServiceIntent);
					serviceStartedLayout();
				}
			}
		});
		listen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				manualActivate();
			}
		});
		hotwordToggle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				prefs.edit()
						.putBoolean("listenHotword",
								!prefs.getBoolean("listenHotword", true))
						.commit();
				serviceStoppedLayout();
			}
		});
		waveToggle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				prefs.edit()
						.putBoolean("wave", !prefs.getBoolean("wave", true))
						.commit();
				serviceStoppedLayout();
			}
		});
	}

	private void manualActivate() {
		Intent intent = new Intent(getApplicationContext(), MyService.class);
		if (!MyService.isRunning) {
			startService(intent);
		}
		intent = new Intent("manual-activation");
		// Add data
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				manualActivationReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				serviceStoppedReceiver);
		super.onPause();
	}

}
