package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

public class ETAExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "E.T.A.",
			"Time of Arrival" };
	private String time;

	public ETAExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, final Context context,
			final AudioUI uiReference) {
		if (!ExecuterUtils.validateInternet(context)) {
			uiReference
					.speak("Sorry calculating ETA requires an active internet connection");
			uiReference.resumeState();
			return;
		}
		if (AudioUI.defaultDestination == null) {
			uiReference.speak("Please set your destination first");
			uiReference.resumeState();
			return;
		}
		final Handler handler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (time == null) {
					uiReference
							.speak("Sorry there was an error calculating your ETA");

				} else {
					uiReference.speak("You will arrive at " + time + " at "
							+ AudioUI.defaultDestination);
				}
				uiReference.resumeState();
				return true;
			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				time = LocationUtils
						.getETA(AudioUI.defaultDestination, context);
				handler.sendEmptyMessage(0);
			}
		}).start();

	}

	@Override
	public void reply(List<String> reply) {

	}
}
