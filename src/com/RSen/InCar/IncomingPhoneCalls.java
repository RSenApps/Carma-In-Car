package com.RSen.InCar;

import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;

public class IncomingPhoneCalls extends PhoneStateListener implements
		SpecialExecuter {
	private Context context;
	private AudioUI uiReference;
	private boolean useBluetooth = false;
	private ITelephony telephonyService;
	private TelephonyManager telephonyManager;

	public void useBluetooth(Boolean useBluetooth) {
		this.useBluetooth = useBluetooth;
	}

	public IncomingPhoneCalls(Context context, AudioUI uiReference) {
		this.context = context;
		this.uiReference = uiReference;
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE);

	}

	public void stop() {
		telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {

		if (TelephonyManager.CALL_STATE_RINGING == state) {
			AudioUI.defaultRecipient = incomingNumber;
			uiReference.interrupt();
			String id = ContactSearcher.numberToID(incomingNumber, context); // ID
																				// is
																				// name>number
			uiReference.speakForReply(id
					+ " is Calling. Would you like to answer?", this);
		} else if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
			uiReference.interrupt();
		} else if (TelephonyManager.CALL_STATE_IDLE == state) {
			uiReference.resumeState();
		}
	}

	@Override
	public void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
	}

	@Override
	public void reply(List<String> reply) {
		if (ExecuterUtils.checkForConfirmation(reply)) {
			try {
				answerPhoneAIDL();
			} catch (Exception e) {
				answerPhoneHeadsethook();
			}
			if (!useBluetooth) {
				enableSpeakerPhone();
			}
		} else {
			// no need to hangup ringer is already silenced
			uiReference.resumeState();
		}
	}

	private void enableSpeakerPhone() {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(true);
	}

	private void answerPhoneAIDL() throws Exception {
		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class c = Class.forName(telephony.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(telephony);
			// telephonyService.silenceRinger();
			telephonyService.answerRingingCall();
		} catch (Exception e) {
			throw e;
		}
	}

	private void answerPhoneHeadsethook() {
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown,
				"android.permission.CALL_PRIVILEGED");

		// froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp,
				"android.permission.CALL_PRIVILEGED");
	}
}
