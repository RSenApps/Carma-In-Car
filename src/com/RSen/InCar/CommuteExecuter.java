package com.RSen.InCar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class CommuteExecuter implements SpecialExecuter {
	private String[] commands = new String[1];
	CommuteMap commuteMap;

	public CommuteExecuter(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String json = prefs.getString("commutes_json", null);

		if (json != null) {
			Gson gson = new Gson();
			commuteMap = gson.fromJson(json, CommuteMap.class);
			Set<String> keySet = commuteMap.keySet();
			if (keySet.size() < 1) {
				return;
			}
			commands = keySet.toArray(commands);
			int index = 0;
			for (String command : commands) {
				commands[index] = "Commute " + command;
				index++;
			}

			CommandRouter.registerMultipleCommands(commands, this);

		}
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		String command = ExecuterUtils.getCommand(inputs, commands);
		command = command.toLowerCase().replace("commute", "").trim();
		Commute commute = commuteMap.get(command);
		if (commute == null) {
			uiReference
					.speak("Sorry no commute was found with name " + command);
			uiReference.resumeState();
			return;
		}
		ArrayList<String> commandList = new ArrayList<String>();
		if (!commute.destination.trim().matches("")) {
			AudioUI.defaultDestination = commute.destination;
			commandList.add("navigate");
			try {
				((NavigationExecuter) CommandRouter
						.getExecuterForCommand("navigate"))
						.executeCommandConfirmation(false, commandList,
								context, uiReference);
			} catch (Exception e) {
			}
		}
		if (commute.readETA) {
			commandList.clear();
			commandList.add("eta");
			CommandRouter.executeCommand(commandList, context, uiReference);
		}
		if (!commute.listenTo.trim().matches("")) {
			commandList.clear();
			commandList.add("listen to " + commute.listenTo);
			try {
				((PlayMusicExecuter) CommandRouter
						.getExecuterForCommand("listen"))
						.executeCommandNoConfirmation(commandList, context,
								uiReference);
			} catch (Exception e) {
			}
		}
		uiReference.resumeState();
	}

	@Override
	public void reply(List<String> reply) {

	}

}
