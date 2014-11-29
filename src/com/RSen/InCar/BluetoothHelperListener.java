package com.RSen.InCar;

public interface BluetoothHelperListener {
	public void startUsingBluetoothHeadset();

	public void headsetDisconnected();

	public void stopUsingBluetoothHeadset();

	public void headsetConnected();
}
