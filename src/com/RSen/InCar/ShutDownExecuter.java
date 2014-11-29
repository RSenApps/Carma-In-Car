package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.content.Intent;

public class ShutDownExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Shutdown",
			"Shut down", "Turn off" };

	public ShutDownExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		Intent intent = new Intent(context, MyService.class);
		context.stopService(intent);
	}

	@Override
	public void reply(List<String> reply) {

	}

}
