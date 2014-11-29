package com.RSen.InCar;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

/**
 * Should handle only speaking of text
 * 
 * @author Ryan
 * 
 */
public class MyTTS implements OnInitListener {
	private TextToSpeech tts;
	private boolean useTTS = false;
	private boolean wasUsingTTS = true;
	Context context;
	private HashMap<String, String> myHashRender = new HashMap<String, String>(); // used
																					// to
																					// change
																					// what
																					// stream
																					// tts
																					// is
																					// spoken
																					// on
	private TTSListener listener;

	public MyTTS(Context context, TTSListener listener) {
		this.context = context;
		this.listener = listener;
		tts = new TextToSpeech(context, MyTTS.this);

	}

	public void stopSpeaking() {
		tts.stop();
	}

	public void stop() {
		tts.shutdown();
	}

	@Override
	public void onInit(int status) {
		// TTS is successfully initialized
		if (status == TextToSpeech.SUCCESS) {
			// Setting speech language
			int result = tts.setLanguage(Locale.US);
			// If your device doesn't support language you set above
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Cook simple toast message with message
				Toast.makeText(context, "Language not supported",
						Toast.LENGTH_LONG).show();
				useTTS = false;
			}
			// Enable the button - It was disabled in main.xml (Go back and
			// Check it)
			else {
				useTTS = true;
			}
			// TTS is not initialized properly
		} else {
			useTTS = false;
		}
		listener.TTSInit();
	}

	public void speak(String whatToSay) {
		if (useTTS) {
			tts.speak(whatToSay, TextToSpeech.QUEUE_FLUSH, myHashRender);
		} else {
			Toast.makeText(context, whatToSay, Toast.LENGTH_SHORT).show();
		}
		while (tts.isSpeaking()) {
			try {
				Thread.sleep(100); // wait for tts to finish
			} catch (InterruptedException e) {
			}
		}
		listener.TTSDoneTalking();
	}

	public void addToQueue(String whatToSay) {
		if (useTTS) {
			tts.speak(whatToSay, TextToSpeech.QUEUE_ADD, myHashRender);
		} else {
			Toast.makeText(context, whatToSay, Toast.LENGTH_SHORT).show();
		}
		while (tts.isSpeaking()) {
			try {
				Thread.sleep(100); // wait for tts to finish
			} catch (InterruptedException e) {
			}
		}
		listener.TTSDoneTalking();
	}

	public void useToastsIfNotBluetooth() {
		if (!useTTS) {
			wasUsingTTS = false;
			return;
		}
		try {
			if (myHashRender.get(TextToSpeech.Engine.KEY_PARAM_STREAM).matches(
					"6")) {
				return;
			}
		} catch (Exception e) {
			;
		}
		useTTS = false;
	}

	public void stopUsingToasts() {
		if (wasUsingTTS) {
			useTTS = true;
		}
	}

	public void changeStream(int newStream) {
		myHashRender.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
				String.valueOf(newStream));
	}
}
