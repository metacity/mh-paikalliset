package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class GlobalApp extends Application {
	static String token = "";
	static String url = "";
	
	static Address startAddress;
	static Address endAddress;
	
	static List<Address> locations = new ArrayList<Address>();
	static List<Route> routes = new ArrayList<Route>();
	
	static String detailsXmlRequest;
}
