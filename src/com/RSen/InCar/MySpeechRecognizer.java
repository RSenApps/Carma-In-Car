package com.RSen.InCar;

import java.lang.ref.WeakReference;
import java.util.List;

import org.apache.commons.codec.language.DoubleMetaphone;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

/**
 * handles only speech recognition (listening, hotword detection
 * 
 * @author Ryan
 * 
 */
public class MySpeechRecognizer implements RecognitionListener {
	// Handler interface
	static final int MSG_RECOGNIZER_START_LISTENING = 1;
	static final int MSG_RECOGNIZER_CANCEL = 2;
	protected final Messenger mServerMessenger = new Messenger(
			new IncomingHandler(this));

	// Statuses
	protected boolean mIsListening;
	protected volatile boolean mIsCountDownOn;
	private boolean listenHotword = false;
	private Context context;
	private boolean listeningPaused = false;
	// Speech Recognition
	protected Intent mSpeechRecognizerIntent;
	protected SpeechRecognizer mSpeechRecognizer;
	private boolean hasBeeped = false;
	private DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
	private final String hotwordEncoded = doubleMetaphone.encode("karma");

	// Audio Manager
	protected AudioManager mAudioManager;

	// callback
	private AudioUI uiReference;

	public MySpeechRecognizer(Context context, AudioUI uiReference) {
		doubleMetaphone.setMaxCodeLen(100);
		this.context = context;
		this.uiReference = uiReference;
		initialize();
	}

	private void initialize() {
		mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		mSpeechRecognizerIntent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(
				RecognizerIntent.EXTRA_CALLING_PACKAGE, context
						.getApplicationContext().getPackageName());
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
				"en-US");
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
		mSpeechRecognizer.setRecognitionListener(this);
	}

	public void listenForHotword() {
		listeningPaused = false;
		listenHotword = true;
		mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
	}

	public void stopListening() {
		listeningPaused = true;
		mSpeechRecognizer.cancel();

	}

	public void stop() {
		try {
			mSpeechRecognizer.destroy();
		} catch (Exception e) {

		}
		mNoSpeechCountDown.cancel();

	}

	public void listen() {
		listeningPaused = false;
		listenHotword = false;
		mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
	}

	// way to turn on/off speech recognizer without beep (hotword recognition)
	protected static class IncomingHandler extends Handler

	{
		private WeakReference<MySpeechRecognizer> mtarget;

		IncomingHandler(MySpeechRecognizer target) {
			mtarget = new WeakReference<MySpeechRecognizer>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				final MySpeechRecognizer target = mtarget.get();
				switch (msg.what) {
				case MSG_RECOGNIZER_START_LISTENING:

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						// turn off beep sound
						target.mAudioManager.setStreamMute(
								AudioManager.STREAM_SYSTEM, true);
					}
					if (!target.mIsListening) {
						target.mSpeechRecognizer
								.startListening(target.mSpeechRecognizerIntent);
						target.mIsListening = true;
						//Log.d(TAG, "message start listening"); //$NON-NLS-1$
					}

					break;

				case MSG_RECOGNIZER_CANCEL:
					target.mSpeechRecognizer.cancel();
					target.mIsListening = false;
					//Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
					break;

				}

			} catch (Exception e) {

			}
		}

	}

	// in jelly bean if there is no speech for an extended period of time it
	// will shut off
	// thus we need something to restart speech recognizer after prolonged time
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {

		@Override
		public void onTick(long millisUntilFinished) {
		}

		@Override
		public void onFinish() {
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
			try {
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			} catch (RemoteException e) {

			}

		}
	};

	@Override
	public void onResults(Bundle results) {
		receiveResults(results);
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		receiveResults(partialResults);
	}

	/**
	 * common method to process any results bundle from
	 * {@link MySpeechRecognizer}
	 */
	private void receiveResults(Bundle results) {
		if ((results != null)
				&& results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
			List<String> heard = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			receiveWhatWasHeard(heard);
		}
	}

	private void receiveWhatWasHeard(List<String> heard) {
		/*
		 * if
		 * (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
		 * "speech_debug", false)) { // DEBUG: String text = ""; for (String
		 * string : heard) { text += string + "\n"; } Toast.makeText(context,
		 * text, Toast.LENGTH_LONG).show(); // DEBUG end }
		 */
		if (listenHotword) {

			// find the target word
			for (String possible : heard) {
				String possibleEncoded = doubleMetaphone.encode(possible);
				if (possibleEncoded.contains(hotwordEncoded)) {
					uiReference.HotwordHeard();
					return;
				}

			}
			// quietly start again
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
			try {
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			} catch (RemoteException e) {

			}
		} else {
			hasBeeped = false;
			uiReference.beep();
			uiReference.SpeechRecognitionReturned(heard);
		}

	}

	@Override
	public void onError(int errorCode) {
		// Log.d("SpeechRecognizer", "Error:" + errorCode);
		if (errorCode == SpeechRecognizer.ERROR_NETWORK) {
			uiReference.speak("No Internet Connection");
		}
		if (!listeningPaused) // prevent restarting if shouldn't be listening
		{
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
			try {
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			} catch (RemoteException e) {

			}
		}
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// Log.d("SpeechRecognizer", "Ready");
		if (!hasBeeped && !listenHotword) {
			// Log.d("SpeechRecognizer", "Beeping");
			hasBeeped = true;
			uiReference.beep();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mIsCountDownOn = true;
			mNoSpeechCountDown.start();

		}

	}

	@Override
	public void onEndOfSpeech() {
		// Log.d("SpeechRecognizer", "End of speech");
	}

	/**
	 * @see android.speech.RecognitionListener#onBeginningOfSpeech()
	 */
	@Override
	public void onBeginningOfSpeech() {
		// Log.d("SpeechRecognizer", "Beginning of speech");
		// speech input will be processed, so there is no need for count down
		// anymore
		if (mIsCountDownOn) {
			mIsCountDownOn = false;
			mNoSpeechCountDown.cancel();
		}
		//Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
	}

	@Override
	public void onRmsChanged(float rmsdB) {
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// Log.d("SpeechRecognizer", "Event: " + eventType);

	}
}
