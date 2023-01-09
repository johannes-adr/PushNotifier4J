package de.jadr.pushnotifier4j;

public class AppToken {
	private final long expires;
	private final String appToken;

	public AppToken(long expires, String appToken) {
		this.expires = expires;
		this.appToken = appToken;
	}

	public long getExpires() {
		return expires;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > expires;
	}

	public String getAppToken() {
		return appToken;
	}

	@Override
	public String toString() {
		return "AppToken [expires=" + expires + ", appToken=" + appToken + " - expired: " + isExpired() + "]";
	}

}
