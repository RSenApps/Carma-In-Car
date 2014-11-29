package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

public class TimeToCurrentDestinationExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "How much longer",
			"Remaining time", "Time to destination" };
	private String time;

	public TimeToCurrentDestinationExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, final Context context,
			final AudioUI uiReference) {
		if (!ExecuterUtils.validateInternet(context)) {
			uiReference
					.speak("Sorry calculating time until arrival requires an active internet connection");
			uiReference.resumeState();
			return;
		}
		if (uiReference.defaultDestination == null) {
			uiReference.speak("Please set your destination first");
			uiReference.resumeState();
			return;
		}
		uiReference.beep();
		final Handler handler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (time == null) {
					uiReference
							.speak("Sorry there was an error calculating time until arrival");

				} else {
					uiReference.speak(time + " until you arrive at "
							+ uiReference.defaultDestination);
				}
				uiReference.resumeState();
				return true;
			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				time = LocationUtils.getTimeToDestination(
						uiReference.defaultDestination, context);
				handler.sendEmptyMessage(0);
			}
		}).start();

	}

	@Override
	public void reply(List<String> reply) {

	}
}
