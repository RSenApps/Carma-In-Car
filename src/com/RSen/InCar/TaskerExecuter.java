package com.RSen.InCar;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class TaskerExecuter implements SpecialExecuter {
	private String[] commands = new String[1];
	HashMap<String, String> taskerMap;

	public TaskerExecuter(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String json = prefs.getString("tasker_json", null);

		if (json != null) {
			Gson gson = new Gson();
			taskerMap = gson.fromJson(json, HashMap.class);
			Set<String> keySet = taskerMap.keySet();
			if (keySet.size() < 1) {
				return;
			}
			commands = keySet.toArray(commands);

			CommandRouter.registerMultipleCommands(commands, this);

		}
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		if (TaskerIntent.testStatus(context).equals(TaskerIntent.Status.OK)) {
			String command = ExecuterUtils.getCommand(inputs, commands);
			TaskerIntent i = new TaskerIntent(taskerMap.get(command));
			context.sendBroadcast(i);
			uiReference.speak("Tasker command: " + command + " executed");
			uiReference.resumeState();
		} else {
			uiReference.speak("Sorry Tasker failed to execute your command");
			uiReference.resumeState();
		}
	}

	@Override
	public void reply(List<String> reply) {
	}

}
