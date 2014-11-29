package com.RSen.InCar;

import java.util.List;

import org.apache.commons.codec.language.DoubleMetaphone;

import android.content.Context;

public class EmailExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Send email",
			"Email" };
	private static String emailSpaced = "";
	private static final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
	private Context context;
	private AudioUI uiReference;
	private boolean waitingForConfirmation = false;
	MessageInfo commandInfo;

	public EmailExecuter() {
		doubleMetaphone.setMaxCodeLen(100);
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		if (!ExecuterUtils.validateInternet(context)) {
			uiReference
					.speak("Sorry sending emails requires an active Internet connection");
			uiReference.resumeState();
			return;
		}
		this.context = context;
		commandInfo = ExecuterUtils.parseMessage(inputs, commands, null,
				context, true);

		this.uiReference = uiReference;
		if (commandInfo == null) {
			uiReference.speak("Sorry I didn't catch who you wanted to email");
			uiReference.resumeState();
			return;
		} else {

			emailSpaced = commandInfo.recipient;
			waitingForConfirmation = true;
			uiReference.speakForReply("Are you sure you would like to email"
					+ emailSpaced + " Subject " + commandInfo.subject
					+ " Message " + commandInfo.message + "?", this);
		}
	}

	// returns String[] of to (0) message (1)

	private void processCommand() {
		try {
			GMailSender sender = new GMailSender("carmaandroid@gmail.com",
					"rJyzbB8Dh6vn");
			sender.sendMail(GMailSender.getUsersEmail(context)
					+ " has a message for you: " + commandInfo.subject,
					commandInfo.message, GMailSender.getUsersEmail(context),
					commandInfo.recipient);
			uiReference.speak("Email sent");
		} catch (Exception e) {
			uiReference.speak("Error email not sent");
		}
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

}
