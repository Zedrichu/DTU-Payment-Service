package utilities;

import messaging.Event;

public class Utils {
	public static void logSend(Event event) {
		System.out.format("[x] publish %s\n", event);
	}
	public static void logHandle(Event event) {
		System.out.format("[x] handle %s\n", event);
	}

}
