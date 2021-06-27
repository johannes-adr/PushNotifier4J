# PushNotifier4J
An unofficial [PushNotifier](https://www.pushnotifier.de) API wrapper written in Java

## Installation (via Maven)
add this to your projects *pom.xml*
````XML
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
````

````XML
<dependency>
  <groupId>com.github.johannes-adr</groupId>
  <artifactId>PushNotifier4J</artifactId>
  <version>V0.1</version>
</dependency>
````

Current version: [![](https://jitpack.io/v/johannes-adr/PushNotifier4J.svg)](https://jitpack.io/#johannes-adr/PushNotifier4J)

## Example
````Java
PushNotifier pn = new PushNotifier("PACKAGE", "API_TOKEN");
pn.login("USERNAME", "PASSWORD");
		
DeviceResponse devices = pn.getDevices();
		
NotificationSendResult result = pn.sendNotification("Hello there","https://www.pushnotifier.de", devices.getAllDevices());
		
if(result.getFailureIds().size() > 0) {
  for(String deviceId : result.getFailureIds()) {
    Device device = devices.getDevicebyId(deviceId);
    System.out.println("Error sending notification to model " + device.getModel());
  }
}else {
  //Sucess
}
````

Everything you need is accessible in an instance of **de.jadr.pushnotifier4j.PushNotifier**
