package com.RSen.InCar;

public class Commute {
	public String destination = "";
	public String listenTo = "";
	public Boolean readETA = false;

	public Commute(String destination, String listenTo, Boolean readETA) {
		this.destination = destination;
		this.listenTo = listenTo;
		this.readETA = readETA;
	}
}
