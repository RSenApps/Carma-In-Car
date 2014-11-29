package com.RSen.InCar;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

/**
 * all ui speaking/listening/waving...etc. handled
 * 
 * @author Ryan
 * 
 */
public class AudioUI implements TTSListener, BluetoothHelperListener,
		WaveControlListener {
	private MySpeechRecognizer speechRecognizer;
	private BluetoothHelper bluetoothHelper;
	private MyTTS myTTS;
	private IncomingPhoneCalls incomingPhoneCalls;
	private IncomingTextMessages incomingTextMessages;
	private WaveController waveController;
	private boolean initialized = false;
	private boolean useBluetooth = false;
	// activation methods
	private boolean listenHotword = false;
	private boolean waveHand = false;
	private boolean callNotifications = false;
	private boolean textNotifications = false;

	private SpecialExecuter waitingForReply;

	public static String defaultDestination;
	public static String defaultRecipient;
	private Context context;
	private HashMap<String, String> correctivePhrases = new HashMap<String, String>();

	public AudioUI(Context context, BluetoothHelper currentBluetoothHelper) {
		this.context = context;
		setVolumes();
		setActivationMethods();
		if (listenHotword || waveHand) {
			WakelockManager.acquireWakelock(context);
		}
		if (waveHand) {
			waveController = new WaveController(context, this);
		}
		if (currentBluetoothHelper == null) {
			bluetoothHelper = new BluetoothHelper(context, this);
			bluetoothHelper.start();
		} else {
			bluetoothHelper = currentBluetoothHelper;
		}
		speechRecognizer = new MySpeechRecognizer(context, this);
		myTTS = new MyTTS(context, this);
		if (callNotifications) {
			incomingPhoneCalls = new IncomingPhoneCalls(context, this);
		}
		if (textNotifications) {
			incomingTextMessages = new IncomingTextMessages(context, this);
		}
		String json = PreferenceManager.getDefaultSharedPreferences(context)
				.getString("correction_phrases_json", null);
		if (json != null) {
			Gson gson = new Gson();
			correctivePhrases = gson.fromJson(json, HashMap.class);
		}
	}

	private void setVolumes() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (am
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * .01 * prefs
				.getInt("volume_media", 50)), 0);
		am.setStreamVolume(6, (int) (am
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * .01 * prefs
				.getInt("volume_bluetooth", 50)), 0); // bluetooth
	}

	private void restoreVolumes() {
		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
	}

	private void setActivationMethods() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		listenHotword = prefs.getBoolean("listenHotword", true);
		waveHand = prefs.getBoolean("wave", true);
		callNotifications = prefs.getBoolean("notifications_call", true);
		textNotifications = prefs.getBoolean("notifications_text", true);
	}

	public void interrupt() {
		myTTS.stopSpeaking();
		speechRecognizer.stopListening();
	}

	public void manualActivation() {
		// wait for initialized
		final Handler handler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				myTTS.stopSpeaking();
				speechRecognizer.stopListening();
				myTTS.speak("Yes?");
				waitingForReply = null;
				speechRecognizer.listen();
				return true;
			}
		});
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (!initialized) {

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {

					}

				}
				handler.sendEmptyMessage(0);
			}
		};
		new Thread(runnable).start();

	}

	public void losingAudioFocus() {
		if (!useBluetooth) {
			if (listenHotword) {
				if (!waveHand) {
					Intent intent = new Intent(context, MyService.class);
					context.stopService(intent);
				}
				listenHotword = false;
				resumeState();
			}
		}
	}

	private void listenForHotword() {
		// Log.d("AudioUI", "ListenForHotword");
		speechRecognizer.listenForHotword();
	}

	private void startUI() {
		if (listenHotword) {
			listenForHotword();
		}
		Toast.makeText(context, "Carma is ready!", Toast.LENGTH_LONG).show();
	}

	public void beep() {
		if (useBluetooth) {
			final ToneGenerator tg = new ToneGenerator(6, 100);
			tg.startTone(ToneGenerator.TONE_PROP_BEEP);
		} else {
			Vibrator vibe = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(50);
		}
	}

	public void addToSpeakQueue(String whatToSay) {
		speechRecognizer.stopListening();
		myTTS.addToQueue(whatToSay);
	}

	public void speak(String whatToSay) {
		Log.d("AudioUI", "Speak:" + whatToSay);
		myTTS.speak(whatToSay);
	}

	public void speakForReply(String whatToSay, SpecialExecuter callBack) {
		Log.d("AudioUI", "SpeakForReply:" + whatToSay);
		myTTS.speak(whatToSay);
		waitingForReply = callBack;
		speechRecognizer.listen();
	}

	public void resumeState() {
		if (listenHotword) {
			listenForHotword();
		} else {
			speechRecognizer.stopListening();
		}
	}

	public void stop() {
		initialized = false;

		WakelockManager.releaseWakelock();
		myTTS.speak("Shutting Down");
		if (callNotifications) {
			incomingPhoneCalls.stop();
		}
		if (textNotifications) {
			incomingTextMessages.stop();
		}
		bluetoothHelper.stop();
		speechRecognizer.stop();
		if (waveHand) {
			waveController.stop();
		}
		myTTS.stop();
		restoreVolumes();
	}

	public void SpeechRecognitionReturned(List<String> results) {
		checkForCorrectivePhrases(results);
		if (waitingForReply == null) {
			Log.d("AudioUI", "Sending command:" + results.get(0));
			// not waiting for reply
			CommandRouter.executeCommand(results, context, this);

		} else {
			if (CommandRouter.checkIfCommand(results)) {
				// Log.d("AudioUI", "Sending command:" + results.get(0));
				CommandRouter.executeCommand(results, context, this);
			} else {
				Log.d("AudioUI", "Replying:" + results.get(0));
				waitingForReply.reply(results);
				waitingForReply = null;
			}
		}
	}

	private void checkForCorrectivePhrases(List<String> results) {
		int index = 0;
		for (String string : results) {
			Set<String> keySet = correctivePhrases.keySet();
			for (String correctivePhrase : keySet) {
				string = string.toLowerCase().replace(
						correctivePhrase.toLowerCase(),
						correctivePhrases.get(correctivePhrase));
			}
			results.set(index, string);
			index++;
		}
	}

	public void HotwordHeard() {
		// Log.d("AudioUI", "HotwordHeard");
		myTTS.speak("Yes?");
		speechRecognizer.listen();
	}

	@Override
	public void TTSInit() {
		// Log.d("AudioUI", "TTSInit");
		initialized = true;
		startUI();
	}

	@Override
	public void TTSDoneTalking() {
		// Log.d("AudioUI", "TTS Done Talking");
	}

	@Override
	public void startUsingBluetoothHeadset() {
		// Log.d("AudioUI", "Using Bluetooth");
		myTTS.changeStream(AudioManager.STREAM_VOICE_CALL); // over bluetooth
		useBluetooth = true;
	}

	@Override
	public void stopUsingBluetoothHeadset() {
		// Log.d("AudioUI", "Not Using Bluetooth");
		myTTS.changeStream(AudioManager.STREAM_MUSIC);
		useBluetooth = false;
	}

	@Override
	public void waveControlActivated() {
		manualActivation();
	}

	public void audioSilenced() {
		myTTS.useToastsIfNotBluetooth();
	}

	public void audioNotSilenced() {
		myTTS.stopUsingToasts();
	}

	@Override
	public void headsetDisconnected() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("stop_on_bluetooth", false)) {
			((MyService) context).stopSelf();
		}
	}

	@Override
	public void headsetConnected() {

	}
}
