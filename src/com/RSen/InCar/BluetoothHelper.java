package com.RSen.InCar;

import android.content.Context;

/**
 * interface to work with bluetooth headset
 * 
 * @author Ryan
 * 
 */
class BluetoothHelper extends BluetoothHeadSetUtils {

	private BluetoothHelperListener listener;

	public BluetoothHelper(Context context, BluetoothHelperListener listener) {
		super(context);
		this.listener = listener;
	}

	@Override
	public void onScoAudioDisconnected() {
		listener.stopUsingBluetoothHeadset();
	}

	@Override
	public void onScoAudioConnected() {
		listener.startUsingBluetoothHeadset();
	}

	@Override
	public void onHeadsetDisconnected() {
		listener.headsetDisconnected();
	}

	@Override
	public void onHeadsetConnected() {
		listener.headsetConnected();
	}
}