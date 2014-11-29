package com.RSen.InCar;

import java.util.List;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;

public class NavigationExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Navigate to",
			"Navigate", "Go to", "Directions to", "Directions for" };
	private String commandInfo;
	private String name;
	private String latlong;
	private Context context;
	private AudioUI uiReference;
	private boolean waitingForConfirmation;

	public NavigationExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, final Context context,
			final AudioUI uiReference) {
		executeCommandConfirmation(true, inputs, context, uiReference);
	}

	public void executeCommandConfirmation(final Boolean waitForConfirmation,
			List<String> inputs, final Context context,
			final AudioUI uiReference) {
		if (!ExecuterUtils.validateInternet(context)) {
			uiReference
					.speak("Sorry navigation requires an active internet connection");
			uiReference.resumeState();
			return;
		}
		commandInfo = ExecuterUtils.getCommandInfo(inputs, commands);
		this.context = context;
		this.uiReference = uiReference;
		if (commandInfo == null) {
			if (AudioUI.defaultDestination != null) {
				commandInfo = AudioUI.defaultDestination;
			} else {
				uiReference
						.speak("Sorry I didn't catch where you wanted to go");
				uiReference.resumeState();
				return;
			}
		}
		uiReference.beep();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (commandInfo.trim().toLowerCase().matches("home")) {
			commandInfo = prefs.getString("home", "home");
		} else if (commandInfo.trim().toLowerCase().matches("work")) {
			commandInfo = prefs.getString("work", "work");
		}
		final Handler validatedHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (waitForConfirmation) {
					waitingForConfirmation = true;
					uiReference.speakForReply(
							"Are you sure you would like to navigate to "
									+ name, NavigationExecuter.this);
				} else {
					processCommand();
				}
				return true;
			}
		});
		final Handler invalidatedHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				uiReference.speak("Sorry no places were found for "
						+ commandInfo);
				uiReference.resumeState();
				return true;
			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] nameLatLong = LocationUtils
						.validateLocationWithWebService(commandInfo, context);
				if (nameLatLong != null) {
					name = nameLatLong[0];
					latlong = nameLatLong[1];
					validatedHandler.sendEmptyMessage(0);
				} else {
					invalidatedHandler.sendEmptyMessage(0);
				}
			}
		}).start();
	}

	private void processCommand() {
		WakelockManager.turnOnScreen(context);
		KeyguardManager keyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
		keyguardLock.disableKeyguard();
		try {
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q=" + latlong));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
					| Intent.FLAG_FROM_BACKGROUND);
			uiReference.speak("Navigating");
			context.startActivity(i);
		} catch (Exception e) {
			uiReference.speak("Sorry no Google Navigation found on device");
		}
		keyguardLock.reenableKeyguard();

	}

	@Override
	public void reply(List<String> reply) {
		if (waitingForConfirmation) {
			waitingForConfirmation = false;
			if (ExecuterUtils.checkForConfirmation(reply)) {
				AudioUI.defaultDestination = name;
				processCommand();
			} else {
				uiReference.speak("Goodbye");
			}
		}
		uiReference.resumeState();
	}
}
