package de.jadr.pushnotifier4j;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @author Jadr (https://github.com/johannes-adr)
 * @version 1.0
 * {@link <ul><li>Github: https://github.com/johannes-adr/PushNotifier4J</li>PushNotifier: https://www.pushnotifier.de<li></ul>}
 * @since 27.06.2021
 */

public class PushNotifier {
	
	private static final int MB_5 = 1024*1024*5;
	
	private static final String BASE_URL = "https://api.pushnotifier.de/v2", LOGIN = "/user/login",
			REFRESH = "/user/refresh", DEVICES = "/devices", TEXT_NOTIFICATION = "/notifications/text",
			URL_NOTIFICATION = "/notifications/url", NOTIFICATION = "/notifications/notification",
			IMAGE_NOTIFICATION = "/notifications/image";

	private static enum Method {
		POST, GET, PUT
	}

	private String _package;
	private final String API_TOKEN;

	private AppToken appToken = null;

	public PushNotifier(String _package, String apiToken) {
		this._package = _package;
		this.API_TOKEN = apiToken;
	}

	public DeviceResponse getDevices() {
		HttpResponse res = doApiCall(Method.GET, DEVICES, null);
		if (res.getStatusCode() == 200) {
			try {
				return new DeviceResponse(new JSONArray(res.readBody()));
			} catch (Exception e) {
				throw new RuntimeException("An unexpected error occured while parsing the login response - " + e);
			}
		} else {
			throw new PushNotifierException("Error fetching device data - " + res.statusMessage);
		}
	}
	
	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-text
	 */
	public NotificationSendResult sendTextNotification(String message, Device... devices) {
		JSONObject jo = new JSONObject();

		ArrayList<String> deviceIds = new ArrayList<String>(devices.length);
		for (Device d : devices)
			deviceIds.add(d.getId());

		jo.put("content", message);
		jo.put("devices", deviceIds);

		HttpResponse res = doApiCall(Method.PUT, TEXT_NOTIFICATION, jo);
		int code = res.getStatusCode();

		switch (code) {
		case 200: {
			return new NotificationSendResult(devices, res.readBodyJSON());
		}
		case 400: {
			throw new PushNotifierException.NotificationExcpetion(
					"Unexpected error - malformed request (" + jo.toString() + ")");
		}
		case 404: {
			throw new PushNotifierException.NotificationExcpetion(
					"Device not found! Refresh the device list with .getDevices()");
		}
		default:
			throw new RuntimeException(res.getStatusCode() + ": " + res.getStatusCode());
		}
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-a-notification
	 */
	public NotificationSendResult sendNotification(String message, String url, Device... devices) {
		JSONObject jo = new JSONObject();

		ArrayList<String> deviceIds = new ArrayList<String>(devices.length);
		for (Device d : devices)
			deviceIds.add(d.getId());

		jo.put("content", message);
		jo.put("devices", deviceIds);
		jo.put("url", url);

		HttpResponse res = doApiCall(Method.PUT, NOTIFICATION, jo);
		int code = res.getStatusCode();

		switch (code) {
		case 200: {
			return new NotificationSendResult(devices, res.readBodyJSON());
		}
		case 400: {
			throw new PushNotifierException.NotificationExcpetion(
					"Unexpected error - malformed request (" + jo.toString() + ")");
		}
		case 404: {
			throw new PushNotifierException.NotificationExcpetion(
					"Device not found! Refresh the device list with .getDevices()");
		}
		default:
			throw new RuntimeException(res.getStatusCode() + ": " + res.getStatusCode());
		}
	}
	
	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-a-url
	 */
	public NotificationSendResult sendUrlNotification(String url, Device... devices) {
		JSONObject jo = new JSONObject();

		ArrayList<String> deviceIds = new ArrayList<String>(devices.length);
		for (Device d : devices)
			deviceIds.add(d.getId());

		jo.put("devices", deviceIds);
		jo.put("url", url);

		HttpResponse res = doApiCall(Method.PUT, URL_NOTIFICATION, jo);
		int code = res.getStatusCode();

		switch (code) {
		case 200: {
			return new NotificationSendResult(devices, res.readBodyJSON());
		}
		case 400: {
			throw new PushNotifierException.NotificationExcpetion(
					"Unexpected error - malformed request (" + jo.toString() + ")");
		}
		case 404: {
			throw new PushNotifierException.NotificationExcpetion(
					"Device not found! Refresh the device list with .getDevices()");
		}
		default:
			throw new RuntimeException(res.getStatusCode() + ": " + res.getStatusCode());
		}
	}
	
	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-an-image
	 * @param fileName: ex. image.png
	 * @throws IOException 
	 * @throws RuntimeException if Imagesize greater or equals than 5mb
	 */
	public NotificationSendResult sendImageNotification(File imagePath, Device... devices) throws IOException {
		byte[] barr = Files.readAllBytes(imagePath.toPath());
		//5 MB
		if(barr.length >= MB_5) {
			throw new RuntimeException("Imagesize is greater or equals than 5 megabytes");
		}
		return this.sendImageNotification(imagePath.getName(), Base64.getEncoder().encodeToString(barr), devices);
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-an-image
	 * @param fileName: ex. image.png
	 * @throws IOException 
	 * @throws RuntimeException if Imagesize greater or equals than 5mb
	 */
	public NotificationSendResult sendImageNotification(String fileName, Image image, Device... devices) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		BufferedImage bimage = null;
		if(image instanceof BufferedImage)bimage = (BufferedImage) image;
		else {
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics g = bimage.getGraphics();
			g.drawImage(image, 0, 0, null);
		}
		ImageIO.write(bimage, fileName.substring(fileName.lastIndexOf(".")+1), baos);
		baos.close();
		byte[] barr = baos.toByteArray();
		//5 MB
		if(barr.length >= MB_5) {
			throw new RuntimeException("Imagesize is greater or equals than 5 megabytes");
		}
		return this.sendImageNotification(fileName, new String(Base64.getEncoder().encodeToString(barr)), devices);
	}
	
	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-an-image
	 * @throws IOException 
	 * @throws RuntimeException if Imagesize greater than 5mb
	 */
	public NotificationSendResult sendImageNotification(String fileName, String imageB64Encoded, Device... devices) {
		JSONObject jo = new JSONObject();
		ArrayList<String> deviceIds = new ArrayList<String>(devices.length);
		for (Device d : devices)
			deviceIds.add(d.getId());
		jo.put("filename", fileName);
		jo.put("devices", deviceIds);
		System.out.println(jo);
		jo.put("content", imageB64Encoded);
		System.out.println(imageB64Encoded);

		HttpResponse res = doApiCall(Method.PUT, IMAGE_NOTIFICATION, jo);
		int code = res.getStatusCode();

		switch (code) {
		case 200: {
			return new NotificationSendResult(devices, res.readBodyJSON());
		}
		case 400: {
			throw new PushNotifierException.NotificationExcpetion(
					"Unexpected error - malformed request (" + jo.toString() + ")");
		}
		case 404: {
			throw new PushNotifierException.NotificationExcpetion(
					"Device not found! Refresh the device list with .getDevices()");
		}
		default:
			throw new RuntimeException(res.getStatusCode() + ": " + res.getStatusMessage());
		}
	}
	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-post-login
	 * @return AppToken if successful
	 * @throws LoginException, RuntimException
	 */
	public AppToken login(String username, String password) {
		HttpResponse res = doApiCall(Method.POST, LOGIN,
				new JSONObject().put("username", username).put("password", password));
		int code = res.code;
		switch (code) {
		case 200: {
			// OK
			try {
				JSONObject response = res.readBodyJSON();
				// expires_at given in seconds -> required in millis
				appToken = new AppToken(response.getLong("expires_at") * 1000, response.getString("app_token"));
				return appToken;
			} catch (Exception e) {
				throw new RuntimeException("An unexpected error occured while parsing the login response - " + e);
			}
		}
		case 404: {
			// Uknown user
			throw new PushNotifierException.LoginException("Give user not found!");
		}
		case 403: {
			// Wrong credentials
			throw new PushNotifierException.LoginException("User found - wrong credentials");
		}
		default:
			throw new RuntimeException("Error: " + code + ": " + res.statusMessage);
		}
	}
	
	public AppToken getCurrentAppToken() {
		return appToken;
	}
	
	public AppToken refreshAppToken() {
		HttpResponse res = doApiCall(Method.GET, REFRESH,null);
		int code = res.code;
		switch (code) {
		case 200: {
			// OK
			try {
				JSONObject response = res.readBodyJSON();
				// expires_at given in seconds -> required in millis
				appToken = new AppToken(response.getLong("expires_at") * 1000, response.getString("app_token"));
				return appToken;
			} catch (Exception e) {
				throw new RuntimeException("An unexpected error occured while parsing the login response - " + e);
			}
		}
		case 404: {
			// Uknown user
			throw new PushNotifierException.LoginException("Give user not found!");
		}
		case 403: {
			// Wrong credentials
			throw new PushNotifierException.LoginException("User found - wrong credentials");
		}
		default:
			throw new RuntimeException("Error: " + code + ": " + res.statusMessage);
		}
	}

	private class HttpResponse{
		private String statusMessage;
		private int code;
		private BufferedInputStream in;
		
		
		
		public String getStatusMessage() {
			return statusMessage;
		}

		public int getStatusCode() {
			return code;
		}

		public HttpResponse(HttpURLConnection conn) throws IOException{
			statusMessage = conn.getResponseMessage();
			code = conn.getResponseCode();
			if(code == 200)in = new BufferedInputStream(conn.getInputStream());
		}
		
		public JSONObject readBodyJSON() {
			return new JSONObject(readBody());
		}
		
		public String readBody() {
			StringBuilder sb = new StringBuilder();
			Scanner sc = new Scanner(in);
			while (sc.hasNext())
				sb.append(sc.nextLine());
			return sb.toString();
		}

		@Override
		public String toString() {
			return "HttpResponse [statusMessage=" + statusMessage + ", code=" + code + "]";
		}
		
		
	}
	
	private HttpResponse doApiCall(Method m, String url, JSONObject body) {
		try {
			URL u = new URL(BASE_URL + url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod(m.toString());

			conn.setRequestProperty("Authorization", "Basic " + enc(_package + ":" + API_TOKEN));
			
			if (appToken != null) {
				if(appToken.isExpired())refreshAppToken();
				conn.setRequestProperty("X-AppToken", appToken.getAppToken());
			}
			
			if (m != Method.GET && body != null) {
				byte[] bodyBytes = body.toString().getBytes("UTF-8");
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
				conn.getOutputStream().write(bodyBytes);
			}
			return new HttpResponse(conn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String enc(String raw) throws UnsupportedEncodingException {
		return Base64.getEncoder().encodeToString(raw.getBytes("UTF-8"));
	}
}
