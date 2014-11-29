package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.util.Log;

public class FeedbackExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Send Feedback",
			"Feedback", "Report Bug" };

	public FeedbackExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		if (!ExecuterUtils.validateInternet(context)) {
			uiReference
					.speak("Sorry sending feedback requires an active internet connection");
			uiReference.resumeState();
			return;
		}
		String commandInfo = ExecuterUtils.getCommandInfo(inputs, commands);
		try {
			GMailSender sender = new GMailSender("carmaandroid@gmail.com",
					"rJyzbB8Dh6vn");
			sender.sendMail("Feedback", commandInfo, "carmaandroid@gmail.com",
					"RSenApps+CarmaFeedback@gmail.com");
		} catch (Exception e) {
			Log.e("FeedbackExecuter", e.getMessage());
		}
		uiReference.speak("Thank you, feedback sent");
		uiReference.resumeState();
	}

	@Override
	public void reply(List<String> reply) {

	}
}
