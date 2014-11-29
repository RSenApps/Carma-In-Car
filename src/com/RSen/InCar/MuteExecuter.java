package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.media.AudioManager;

public class MuteExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Mute", "Silence" };

	public MuteExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		uiReference.speak("Silencing audio");
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		uiReference.audioSilenced();
		uiReference.resumeState();
	}

	@Override
	public void reply(List<String> reply) {

	}

}
