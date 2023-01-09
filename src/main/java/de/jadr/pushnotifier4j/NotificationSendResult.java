package de.jadr.pushnotifier4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationSendResult {

	private final Device[] devices;
	private final List<String> successIds;
	private final List<String> failureIds;

	public NotificationSendResult(Device[] devices, String responseBody) {
		this.devices = devices;

		final JSONObject jo = new JSONObject(responseBody);

		final JSONArray success = jo.getJSONArray("success");
		final JSONArray error = jo.getJSONArray("error");

		this.successIds = new ArrayList<>(success.length());
		this.failureIds = new ArrayList<>(error.length());

		for (int i = 0; i < success.length(); i++) {
			final JSONObject device = success.getJSONObject(i);
			successIds.add(device.getString("device_id"));
		}

		for (int i = 0; i < error.length(); i++) {
			final JSONObject device = success.getJSONObject(i);
			failureIds.add(device.getString("device_id"));
		}
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
