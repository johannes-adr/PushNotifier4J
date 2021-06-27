package de.jadr.pushnotifier4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public class NotificationSendResult {

	private final Device[] devices;
	private final List<String> successIds;
	private final List<String> failureIds;

	public NotificationSendResult(Device[] devices, JSONObject jo) {
		this.devices = devices;
		
		List<Object> success = jo.getJSONArray("success").toList();
		List<Object> error = jo.getJSONArray("error").toList();
		
		this.successIds = new ArrayList<String>(success.size());
		this.failureIds = new ArrayList<String>(error.size());
		for (Object o : success)successIds.add(((HashMap<String, String>)o).get("device_id"));
		for (Object o : error)failureIds.add(((HashMap<String, String>)o).get("device_id"));
	}
	
	public Device[] getDevices() {
		return devices;
	}

	/**
	 * @return List of device IDs that the notification has been sent to;
	 */
	public List<String> getSuccessIds() {
		return successIds;
	}

	/**
	 * @return List of device IDs that the notification has not been sent to;
	 */
	public List<String> getFailureIds() {
		return failureIds;
	}

	@Override
	public String toString() {
		return "NotificationSendResult [successIds=" + successIds
				+ ", failureIds=" + failureIds + "]";
	}
	
	
}
