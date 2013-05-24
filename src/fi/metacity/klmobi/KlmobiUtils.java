package fi.metacity.klmobi;

import java.text.SimpleDateFormat;

public class KlmobiUtils {
	public static final SimpleDateFormat time_format = new SimpleDateFormat("HH:mm");
	public static final SimpleDateFormat datetime_format = new SimpleDateFormat("yyyyMMdd;HHmm");
	

	public static String niceTimeFormat(int hour, int minute) {
		return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
	}
	
	public static String niceDateFormat(int day, int month, int year) {
		return (day < 10 ? "0" + day : day) + "/" 
				+ (month+1 < 10 ? "0" + (month+1) : (month+1)) + "/"
				+ year;
	}

}
