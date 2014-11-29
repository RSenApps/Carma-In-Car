package com.RSen.InCar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class MyService extends Service {
	public static boolean isRunning = false;
	AudioUI ui;
	private BluetoothHelper helper;
	private BroadcastReceiver manualActivationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			manualActivation();
		}
	};

	public MyService() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!isRunning) {
			Toast.makeText(this, "Carma, warming up...", Toast.LENGTH_SHORT)
					.show();
			listenForManualActivation();
			isRunning = true;

			LocalBroadcastManager.getInstance(this).sendBroadcast(
					new Intent("service-started"));
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher;
			notification.flags = Notification.FLAG_ONGOING_EVENT
					| Notification.PRIORITY_LOW;

			notification.setLatestEventInfo(this, "Service Running",
					"Click to stop", PendingIntent.getActivity(this, 12343,
							new Intent(this, MainActivity.class), 0));
			startForeground(12342, notification);
			CommandRouter.cleanCommands();
			new TaskerExecuter(this);
			new MuteExecuter();
			new ControlMusicExecuter();
			new NavigationExecuter();
			new CallExecuter();
			new ETAExecuter();
			new SendETAExecuter();
			new SetDestinationExecuter();
			new TimeToCurrentDestinationExecuter();
			new DistanceToCurrentDestinationExecuter();
			new SMSExecuter();
			new CommuteExecuter(this);
			new CancelExecuter();
			new ShutDownExecuter();
			new EmailExecuter();
			new PlayMusicExecuter();
			new FeedbackExecuter();
			new UnMuteExecuter();
			ui = new AudioUI(this, helper);
			// initialize specialexecuters

		}

		return Service.START_STICKY;
	}

	private void listenForManualActivation() {
		LocalBroadcastManager.getInstance(this)
				.registerReceiver(manualActivationReceiver,
						new IntentFilter("manual-activation"));
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				manualActivationReceiver);
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent("service-stopped"));
		isRunning = false;
		stopForeground(true);
		ui.stop();
		super.onDestroy();
	}

	private void manualActivation() {
		// wait for initialized
		final Handler handler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				ui.manualActivation();
				return true;
			}
		});
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (ui == null) {

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

	@Override
	public IBinder onBind(Intent arg0) {
		throw new UnsupportedOperationException();
	}

}
