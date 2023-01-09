package de.jadr.pushnotifier4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class DeviceResponse {

	private final Map<String, Device> deviceMap = new HashMap<>();

	public DeviceResponse(JSONArray arr) {
		for (int i = 0; i < arr.length(); i++) {
			Device device = new Device(arr.getJSONObject(i));
			deviceMap.put(device.getId(), device);
		}
	}

	public Device getDevicebyId(String id) {
		return deviceMap.get(id);
	}

	public Device[] getAllDevices() {
		Collection<Device> devices = deviceMap.values();
		return devices.toArray(new Device[devices.size()]);
	}

}
