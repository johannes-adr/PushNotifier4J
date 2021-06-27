package de.jadr.pushnotifier4j;

import java.util.Collection;
import java.util.HashMap;

import org.json.JSONArray;

public class DeviceResponse {
	
	private final HashMap<String, Device> MAP = new HashMap<String, Device>();
	
	public DeviceResponse(JSONArray arr) {
		for(int i = 0;i < arr.length();i++) {
			Device device = new Device(arr.getJSONObject(i));
			MAP.put(device.getId(), device);
		}
	}
	
	public Device getDevicebyId(String id) {
		return MAP.get(id);
	}
	
	public Device[] getAllDevices() {
		Collection<Device> devices = MAP.values();
		return devices.toArray(new Device[devices.size()]);
	}
	
}
