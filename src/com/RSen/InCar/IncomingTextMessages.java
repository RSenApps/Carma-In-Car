package com.RSen.InCar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class IncomingTextMessages extends BroadcastReceiver {

	private AudioUI uiReference;
	private Context context;

	public IncomingTextMessages(Context context, AudioUI uiReference) {
		this.uiReference = uiReference;
		this.context = context;
		IntentFilter filter = new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED");
		context.registerReceiver(this, filter);
	}

	public void stop() {
		context.unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction()
				.equals("android.provider.Telephony.SMS_RECEIVED")) {
			uiReference.interrupt();
			Bundle bundle = intent.getExtras(); // ---get the SMS message passed
												// in---
			SmsMessage[] msgs = null;
			String msg_from;
			if (bundle != null) {
				// ---retrieve the SMS message received---
				try {
					Object[] pdus = (Object[]) bundle.get("pdus");
					msgs = new SmsMessage[pdus.length];
					for (int i = 0; i < msgs.length; i++) {
						msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						msg_from = msgs[i].getOriginatingAddress();
						AudioUI.defaultRecipient = msg_from;
						String msgBody = msgs[i].getMessageBody();
						String id = ContactSearcher.numberToID(msg_from,
								context);

						uiReference.addToSpeakQueue(id + " texted " + msgBody);
					}
				} catch (Exception e) {
					// Log.d("Exception caught",e.getMessage());
				}
			}
			uiReference.resumeState();
		}
	}

}
