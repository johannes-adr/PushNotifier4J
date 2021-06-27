package de.jadr.pushnotifier4j;

import org.json.JSONObject;

public class Device {
	private final String id;
	private final String title;
	private final String model;
	private final String image;
	
	public Device(String id, String title, String model, String image) {
		super();
		this.id = id;
		this.title = title;
		this.model = model;
		this.image = image;
	}
	
	public Device(JSONObject dj) {
		id = dj.getString("id");
		title = dj.getString("title");
		model = dj.getString("model");
		image = dj.getString("image");
	}
	
	@Override
	public String toString() {
		return "Device [id=" + id + ", title=" + title + ", model=" + model +"]";
	}
	public String getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getModel() {
		return model;
	}
	public String getImageUrl() {
		return image;
	}
}
