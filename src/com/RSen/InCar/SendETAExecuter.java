package com.RSen.InCar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.telephony.SmsManager;

public class SendETAExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Send E.T.A. to",
			"Send E.T.A." };
	private String time;
	private String recipient;
	private AudioUI uiReference;

	public SendETAExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, final Context context,
			final AudioUI uiReference) {
		this.uiReference = uiReference;
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
		recipient = ExecuterUtils.getCommandInfo(inputs, commands);
		recipient = ExecuterUtils.getPhoneNumber(recipient, context);
		if (recipient == null && AudioUI.defaultRecipient != null) {
			recipient = AudioUI.defaultRecipient;
		}
		if (recipient == null) {
			uiReference
					.speak("Sorry I didn't catch who you wanted to send your ETA to");
			uiReference.resumeState();
			return;
		}
		uiReference.beep();

		final Handler handler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (time == null) {
					uiReference
							.speak("Sorry there was an error calculating your E.T.A.");
					uiReference.resumeState();
				} else {
					uiReference.speakForReply(
							"Are you sure you would like to send your E.T.A. to "
									+ "location " + AudioUI.defaultDestination
									+ " to " + recipient, SendETAExecuter.this);
				}

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
		if (ExecuterUtils.checkForConfirmation(reply)) {
			processCommand();
		} else {
			uiReference.speak("Goodbye");
		}
		uiReference.resumeState();
	}

	private void processCommand() {
		sendSMS(recipient, "ETA: " + time
				+ " - Sent via Carma In-Car for Android");
		uiReference.speak("Text Sent");
	}

	private void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
	}
}
