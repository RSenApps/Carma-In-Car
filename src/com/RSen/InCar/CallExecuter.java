package com.RSen.InCar;

import java.util.List;

import org.apache.commons.codec.language.DoubleMetaphone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class CallExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Call number",
			"Dial number", "Call", "Dial" };
	private static String phoneNumber;
	private static String phoneNumberSpaced = "";
	private static final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
	private Context context;
	private AudioUI uiReference;
	private boolean waitingForConfirmation = false;

	public CallExecuter() {
		doubleMetaphone.setMaxCodeLen(15); // arbitrary number
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {

		this.context = context;
		phoneNumber = getPhoneNumber(inputs);
		this.uiReference = uiReference;
		if (phoneNumber == null) {
			if (AudioUI.defaultRecipient != null) {
				phoneNumber = AudioUI.defaultRecipient;

			} else {
				uiReference
						.speak("Sorry I didn't catch what number you wanted to call");
				uiReference.resumeState();
				return;
			}
		}

		phoneNumberSpaced = ""; // used to prevent text to speech from
								// combining numbers
		for (char c : phoneNumber.toCharArray()) {
			phoneNumberSpaced += c + " ";
		}
		waitingForConfirmation = true;
		uiReference.speakForReply("Are you sure you would like to call "
				+ phoneNumberSpaced + "?", this);

	}

	private void processCommand() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + phoneNumber));
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		uiReference.speak("Calling");
		context.startActivity(callIntent);
	}

	@Override
	public void reply(List<String> reply) {
		if (waitingForConfirmation) {
			waitingForConfirmation = false;
			if (ExecuterUtils.checkForConfirmation(reply)) {
				AudioUI.defaultRecipient = phoneNumber;
				processCommand();
			} else {
				uiReference.speak("Goodbye");

			}
		}
		uiReference.resumeState();
	}

	private String getPhoneNumber(List<String> inputs) {
		for (String input : inputs) {
			String inputEncoded = doubleMetaphone.encode(input);

			for (String command : commands) {
				String commandEncoded = doubleMetaphone.encode(command);
				if (inputEncoded.startsWith(commandEncoded)) {
					int commandLength = command.length();
					String commandInfo = input.substring(commandLength).trim();
					if (commandInfo.length() == 0) {
						return null;
					}
					return ExecuterUtils.getPhoneNumber(commandInfo, context);

				}
			}
		}
		return null;

	}

}
