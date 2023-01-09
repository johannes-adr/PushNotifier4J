package de.jadr.pushnotifier4j;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * @author Jadr (https://github.com/johannes-adr)
 * @version 1.0
 * {@link <ul><li>Github: https://github.com/johannes-adr/PushNotifier4J</li>PushNotifier: https://www.pushnotifier.de<li></ul>}
 * @since 27.06.2021
 */

public class PushNotifier {

	private static final int MB_5 = 1024 * 1024 * 5;

	private static final String BASE_URL = "https://api.pushnotifier.de/v2";
	private static final String LOGIN = "/user/login";
	private static final String REFRESH = "/user/refresh";
	private static final String DEVICES = "/devices";
	private static final String TEXT_NOTIFICATION = "/notifications/text";
	private static final String URL_NOTIFICATION = "/notifications/url";
	private static final String NOTIFICATION = "/notifications/notification";
	private static final String IMAGE_NOTIFICATION = "/notifications/image";

	private enum Method {
		POST, GET, PUT
	}

	private final String packageName;
	private final String apiToken;

	private AppToken appToken = null;

	public PushNotifier(String packageName, String apiToken) {
		this.packageName = packageName;
		this.apiToken = apiToken;
	}

	public DeviceResponse getDevices() {
		final HttpResponse<String> res = doApiCall(Method.GET, DEVICES, null);

		if (res.statusCode() == 200) {
			try {
				return new DeviceResponse(new JSONArray(res.body()));
			} catch (JSONException e) {
				throw new PushNotifierException("An unexpected error occured while parsing the login response - " + e);
			}
		} else {
			throw new PushNotifierException("Error fetching device data - " + res.statusCode());
		}
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-text
	 */
	public NotificationSendResult sendTextNotification(String message, Device... devices) {
		final JSONObject jo = new JSONObject();

		final List<String> deviceIds = new ArrayList<>(devices.length);
		for (Device d : devices) {
			deviceIds.add(d.getId());
		}

		jo.put("content", message);
		jo.put("devices", deviceIds);

		final HttpResponse<String> res = doApiCall(Method.PUT, TEXT_NOTIFICATION, jo);

		switch (res.statusCode()) {
			case 200: {
				return new NotificationSendResult(devices, res.body());
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
				throw new PushNotifierException(String.valueOf(res.statusCode()));
		}
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-a-notification
	 */
	public NotificationSendResult sendNotification(String message, String url, Device... devices) {
		final JSONObject jo = new JSONObject();

		final List<String> deviceIds = new ArrayList<>(devices.length);
		for (Device d : devices) {
			deviceIds.add(d.getId());
		}

		jo.put("content", message);
		jo.put("devices", deviceIds);
		jo.put("url", url);

		final HttpResponse<String> res = doApiCall(Method.PUT, NOTIFICATION, jo);

		switch (res.statusCode()) {
			case 200: {
				return new NotificationSendResult(devices, res.body());
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
				throw new PushNotifierException(String.valueOf(res.statusCode()));
		}
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-a-url
	 */
	public NotificationSendResult sendUrlNotification(String url, Device... devices) {
		final JSONObject jo = new JSONObject();

		final List<String> deviceIds = new ArrayList<>(devices.length);
		for (Device d : devices) {
			deviceIds.add(d.getId());
		}

		jo.put("devices", deviceIds);
		jo.put("url", url);

		final HttpResponse<String> res = doApiCall(Method.PUT, URL_NOTIFICATION, jo);

		switch (res.statusCode()) {
			case 200: {
				return new NotificationSendResult(devices, res.body());
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
				throw new PushNotifierException(String.valueOf(res.statusCode()));
		}
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-an-image
	 *
	 * @param imagePath: ex. image.png
	 * @throws IOException
	 * @throws RuntimeException if Imagesize greater or equals than 5mb
	 */
	public NotificationSendResult sendImageNotification(File imagePath, Device... devices) throws IOException {
		byte[] barr = Files.readAllBytes(imagePath.toPath());
		//5 MB
		if (barr.length >= MB_5) {
			throw new PushNotifierException("Imagesize is greater or equals than 5 megabytes");
		}
		return this.sendImageNotification(imagePath.getName(), Base64.getEncoder().encodeToString(barr), devices);
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-an-image
	 *
	 * @param fileName: ex. image.png
	 * @throws IOException
	 * @throws RuntimeException if Imagesize greater or equals than 5mb
	 */
	public NotificationSendResult sendImageNotification(String fileName, Image image, Device... devices) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		BufferedImage bimage = null;
		if (image instanceof BufferedImage) {
			bimage = (BufferedImage) image;
		} else {
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics g = bimage.getGraphics();
			g.drawImage(image, 0, 0, null);
		}
		ImageIO.write(bimage, fileName.substring(fileName.lastIndexOf(".") + 1), baos);
		baos.close();
		byte[] barr = baos.toByteArray();
		//5 MB
		if (barr.length >= MB_5) {
			throw new PushNotifierException("Imagesize is greater or equals than 5 megabytes");
		}
		return this.sendImageNotification(fileName, Base64.getEncoder().encodeToString(barr), devices);
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-put-send-an-image
	 *
	 * @throws IOException
	 * @throws RuntimeException if Imagesize greater than 5mb
	 */
	public NotificationSendResult sendImageNotification(String fileName, String imageB64Encoded, Device... devices) {
		final JSONObject jo = new JSONObject();
		final List<String> deviceIds = new ArrayList<>(devices.length);
		for (Device d : devices) {
			deviceIds.add(d.getId());
		}
		jo.put("filename", fileName);
		jo.put("devices", deviceIds);
		System.out.println(jo);
		jo.put("content", imageB64Encoded);
		System.out.println(imageB64Encoded);

		final HttpResponse<String> res = doApiCall(Method.PUT, IMAGE_NOTIFICATION, jo);

		switch (res.statusCode()) {
			case 200: {
				return new NotificationSendResult(devices, res.body());
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
				throw new PushNotifierException(String.valueOf(res.statusCode()));
		}
	}

	/**
	 * https://api.pushnotifier.de/v2/doc/#endpoint-post-login
	 *
	 * @return AppToken if successful
	 * @throws PushNotifierException.LoginException, RuntimException
	 */
	public AppToken login(String username, String password) {
		final HttpResponse<String> res = doApiCall(Method.POST, LOGIN,
				new JSONObject().put("username", username).put("password", password));

		switch (res.statusCode()) {
			case 200: {
				// OK
				try {
					JSONObject response = new JSONObject(res.body());
					// expires_at given in seconds -> required in millis
					appToken = new AppToken(response.getLong("expires_at") * 1000, response.getString("app_token"));
					return appToken;
				} catch (JSONException e) {
					throw new PushNotifierException("An unexpected error occured while parsing the login response - " + e);
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
				throw new PushNotifierException("Error: " + res.statusCode());
		}
	}

	public AppToken getCurrentAppToken() {
		return appToken;
	}

	public AppToken refreshAppToken() {
		final HttpResponse<String> res = doApiCall(Method.GET, REFRESH, null);

		switch (res.statusCode()) {
			case 200: {
				// OK
				try {
					final JSONObject response = new JSONObject(res.body());
					// expires_at given in seconds -> required in millis
					appToken = new AppToken(response.getLong("expires_at") * 1000, response.getString("app_token"));
					return appToken;
				} catch (JSONException e) {
					throw new PushNotifierException("An unexpected error occured while parsing the login response - " + e);
				}
			}
			case 404: {
				// Uknown user
				throw new PushNotifierException.LoginException("Given user not found!");
			}
			case 403: {
				// Wrong credentials
				throw new PushNotifierException.LoginException("User found - wrong credentials");
			}
			default:
				throw new PushNotifierException("Error: " + res.statusCode());
		}
	}

	private HttpResponse<String> doApiCall(Method method, String url, JSONObject body) {
		final HttpClient client = HttpClient.newHttpClient();

		final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + url))
				.header("Authorization", "Basic " + enc(packageName + ":" + apiToken));

		if (appToken != null) {
			if (appToken.isExpired()) {
				refreshAppToken();
			}
			httpRequestBuilder.header("X-AppToken", appToken.getAppToken());
		}

		if (method == Method.GET) {
			httpRequestBuilder.GET();
		} else {
			httpRequestBuilder.method(method.toString(), HttpRequest.BodyPublishers.ofString(body.toString()));
			httpRequestBuilder.header("Content-Type", "application/json");
		}

		final HttpRequest request = httpRequestBuilder.build();

		try {
			return client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			throw new PushNotifierException(e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PushNotifierException(e.getMessage());
		}
	}

	private static String enc(String raw) {
		return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
	}
}
