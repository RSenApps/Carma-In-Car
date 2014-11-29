package com.RSen.InCar;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.language.DoubleMetaphone;

import android.content.Context;
import android.telephony.SmsManager;

public class SMSExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Text",
			"Reply with text", "Send text", "Send message", "Send sms" };
	private static String phoneNumberSpaced = "";
	private static final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
	private AudioUI uiReference;
	private boolean waitingForConfirmation = false;
	MessageInfo commandInfo;

	public SMSExecuter() {
		doubleMetaphone.setMaxCodeLen(100);
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		int index = 0;
		for (String string : inputs) {
			if (string.toLowerCase().startsWith("text")) {
				string = string.toLowerCase().replaceFirst("text", "text to");
			}
			inputs.set(index, string);
			index++;
		}
		commandInfo = ExecuterUtils.parseMessage(inputs, commands,
				AudioUI.defaultRecipient, context, false);

		this.uiReference = uiReference;
		if (commandInfo == null) {
			uiReference
					.speak("Sorry I didn't catch what number you wanted to text");
			uiReference.resumeState();
			return;

		} else {
			phoneNumberSpaced = ""; // used to prevent text to speech from
									// combining numbers
			for (char c : commandInfo.recipient.toCharArray()) {
				phoneNumberSpaced += c + " ";
			}
			waitingForConfirmation = true;
			uiReference.speakForReply("Are you sure you would like to text "
					+ commandInfo.message + " to " + phoneNumberSpaced + "?",
					this);
			commandInfo.message += " - Sent via Carma In-Car for Android";
		}
	}

	private void processCommand() {
		sendSMS(commandInfo.recipient, commandInfo.message);
		uiReference.speak("Text Sent");
	}

	@Override
	public void reply(List<String> reply) {
		if (waitingForConfirmation) {
			waitingForConfirmation = false;
			if (ExecuterUtils.checkForConfirmation(reply)) {
				AudioUI.defaultRecipient = commandInfo.recipient;
				processCommand();
			} else {
				uiReference.speak("Goodbye");
			}
		}
		uiReference.resumeState();
	}

	private void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
	}

}
