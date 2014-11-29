package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.media.AudioManager;

public class UnMuteExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Unmute" };

	public UnMuteExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		uiReference.speak("Turning on audio");
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager
				.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * .5), 0);
		uiReference.audioNotSilenced();
		uiReference.resumeState();
	}

	@Override
	public void reply(List<String> reply) {

	}

}
