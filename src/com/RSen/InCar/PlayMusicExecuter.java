package com.RSen.InCar;

import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;

public class PlayMusicExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Listen to",
			"Listen" };
	private String query = "";
	private Context context;
	AudioUI uiReference;

	public PlayMusicExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context mcontext,
			AudioUI uiReference) {
		this.uiReference = uiReference;
		context = mcontext;
		query = ExecuterUtils.getCommandInfo(inputs, commands);
		uiReference.speakForReply("Would you like to listen to " + query, this);

	}

	public void executeCommandNoConfirmation(List<String> inputs,
			Context mcontext, AudioUI uiReference) {
		this.uiReference = uiReference;
		context = mcontext;
		query = ExecuterUtils.getCommandInfo(inputs, commands);

		try {
			uiReference.speak("Starting " + query);
			uiReference.losingAudioFocus();
			Intent intent = new Intent(
					"android.media.action.MEDIA_PLAY_FROM_SEARCH");
			intent.putExtra(SearchManager.QUERY, query);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_FROM_BACKGROUND);
			context.startActivity(intent);
		} catch (Exception e) {
			uiReference.speak("Sorry no supported music player found");
		}

	}

	@Override
	public void reply(List<String> reply) {
		if (ExecuterUtils.checkForConfirmation(reply)) {
			try {
				uiReference.speak("Starting " + query);
				uiReference.losingAudioFocus();
				Intent intent = new Intent(
						"android.media.action.MEDIA_PLAY_FROM_SEARCH");
				intent.putExtra(SearchManager.QUERY, query);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK
						| Intent.FLAG_FROM_BACKGROUND);
				context.startActivity(intent);
			} catch (Exception e) {
				uiReference.speak("Sorry no supported music player found");
			}

		} else {
			uiReference.speak("Goodbye");
		}
		uiReference.resumeState();
	}

}
