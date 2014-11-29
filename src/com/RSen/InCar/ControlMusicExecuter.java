package com.RSen.InCar;

import java.util.List;

import android.content.Context;
import android.os.IBinder;
import android.view.KeyEvent;

public class ControlMusicExecuter implements SpecialExecuter {
	private static final String[] commands = new String[] { "Pause", "Skip",
			"Previous", "Resume", "Play" };

	public ControlMusicExecuter() {
		CommandRouter.registerMultipleCommands(commands, this);
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		String command = ExecuterUtils.getCommand(inputs, commands);
		uiReference.speak(command);

		int keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
		if (command.matches("Skip")) {
			keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
		} else if (command.matches("Previous")) {
			keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
		}
		uiReference.resumeState();
		sendAction(keyCode, context);

	}

	public void sendAction(int keyCode, Context context) {
		/*
		 * Attempt to execute the following with reflection.
		 * 
		 * [Code] IAudioService audioService =
		 * IAudioService.Stub.asInterface(b);
		 * audioService.dispatchMediaKeyEvent(keyEvent);
		 */
		try {

			// Get binder from ServiceManager.checkService(String)
			IBinder iBinder = (IBinder) Class
					.forName("android.os.ServiceManager")
					.getDeclaredMethod("checkService", String.class)
					.invoke(null, Context.AUDIO_SERVICE);

			// get audioService from IAudioService.Stub.asInterface(IBinder)
			Object audioService = Class
					.forName("android.media.IAudioService$Stub")
					.getDeclaredMethod("asInterface", IBinder.class)
					.invoke(null, iBinder);

			// Dispatch keyEvent using
			// IAudioService.dispatchMediaKeyEvent(KeyEvent)
			Class.forName("android.media.IAudioService")
					.getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
					.invoke(audioService, new KeyEvent(0, keyCode));
			Class.forName("android.media.IAudioService")
					.getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
					.invoke(audioService, new KeyEvent(1, keyCode));

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void reply(List<String> reply) {
	}

}
