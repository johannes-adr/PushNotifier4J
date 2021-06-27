package de.jadr.test;

import de.jadr.pushnotifier4j.DeviceResponse;
import de.jadr.pushnotifier4j.NotificationSendResult;
import de.jadr.pushnotifier4j.PushNotifier;

public class Example {
	public static void main(String[] args) {
		
		PushNotifier pn = new PushNotifier("PACKAGE", "API_TOKEN");
		pn.login("USERNAME", "PASSWORD");
		
		DeviceResponse devices = pn.getDevices();
		
		
		NotificationSendResult result = pn.sendNotification("Hello there","https://www.pushnotifier.de", devices.getDevicebyId("Y63n"));
		if(result.getFailureIds().size() > 0) {
			//Error
		}else {
			//Success
		}
		
	}
}
