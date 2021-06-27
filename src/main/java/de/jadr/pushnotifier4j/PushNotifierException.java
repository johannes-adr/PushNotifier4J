package de.jadr.pushnotifier4j;

public class PushNotifierException extends RuntimeException{
	
	public PushNotifierException(String msg) {
		super(msg);
	}
	
	public static class LoginException extends PushNotifierException{
		public LoginException(String msg) {
			super(msg);
		}
	}
	
	public static class NotificationExcpetion extends PushNotifierException{

		public NotificationExcpetion(String msg) {
			super(msg);
		}
		
	}

}
