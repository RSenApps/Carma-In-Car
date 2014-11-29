package com.RSen.InCar;

import java.util.List;

import android.content.Context;

public class CancelExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Cancel",
			"Nevermind", "Never mind" };

	public CancelExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		uiReference.speak("Goodbye");
		uiReference.resumeState();
	}

	@Override
	public void reply(List<String> reply) {

	}

}
