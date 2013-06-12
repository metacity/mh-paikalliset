package fi.metacity.klmobi;

import org.json.JSONObject;

public class Address {
	final JSONObject json;
	
	public Address(JSONObject json) {
		this.json = json;
	}
	
	public String toString() {
		if (json.isNull("name")) {
			return (int)Float.parseFloat(json.optString("x")) + "; " 
					+ (int)Float.parseFloat(json.optString("y"));
		}
		return json.optString("name") + streetNumber() + ", " + json.optString("city");
	}

	public String streetOnly() {
		if (json.isNull("name")) {
			return toString();
		} 
		return json.optString("name") + streetNumber();
	}
	
	private String streetNumber()  {
		if (json.isNull("number")) {
			return "";
		}
		return " " + json.optString("number");
	}
}
