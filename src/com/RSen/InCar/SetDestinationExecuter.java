package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

public class SetDestinationExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] {
			"Set destination to", "Set this nation to", "Set destination as",
			"Set this nation as", "Set definition to", "Set destination",
			"Set this nation", "Set definition" };
	private String destination;

	public SetDestinationExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, final Context context,
			final AudioUI uiReference) {
		if (!ExecuterUtils.validateInternet(context)) {
			uiReference
					.speak("Sorry navigation requires an active internet connection");
			uiReference.resumeState();
			return;
		}
		destination = ExecuterUtils.getCommandInfo(inputs, commands);
		if (destination == null) {
			uiReference.speak("Sorry I didn't catch where you wanted to go");
			uiReference.resumeState();
			return;
		}
		uiReference.beep();
		final Handler validatedHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				AudioUI.defaultDestination = destination;
				uiReference.speak("Setting destination to " + destination);
				uiReference.resumeState();
				return true;
			}
		});
		final Handler invalidatedHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				uiReference.speak("Sorry no places were found for "
						+ destination);
				uiReference.resumeState();
				return true;
			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] nameLatLong = LocationUtils
						.validateLocationWithWebService(destination, context);
				if (nameLatLong != null) {
					destination = nameLatLong[0];
					validatedHandler.sendEmptyMessage(0);
				} else {
					invalidatedHandler.sendEmptyMessage(0);
				}
			}
		}).start();
	}

	@Override
	public void reply(List<String> reply) {

	}
}
