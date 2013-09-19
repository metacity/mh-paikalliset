package fi.metacity.klmobi;

import org.json.JSONObject;

public class Address {
	final JSONObject json;
	
	public Address(JSONObject json) {
		this.json = json;
	}
	
	public String toString() {
		if (!json.isNull("user_given_name")) {
			return json.optString("user_given_name") + " [" + shortAddress() + "]";
		}
		return fullAddress();
	}
	
	public String userGivenName() {
		if (json.isNull("user_given_name")) {
			return "";
		}
		return json.optString("user_given_name");
	}
	
	public String fullAddress() {
		if (!json.isNull("name")) {
			return json.optString("name") + streetNumber() + ", " + json.optString("city");
		}
		return coordinatesOnly();
	}
	
	public String shortName() {
		if (!json.isNull("user_given_name")) {
			return json.optString("user_given_name");
		}
		return shortAddress();
	}

	public String shortAddress() {
		if (!json.isNull("name")) {
			return json.optString("name") + streetNumber();
		}
		return coordinatesOnly(); 
	}
	
	public String coordinatesOnly() {
		return (int)Float.parseFloat(json.optString("x")) + "; " 
				+ (int)Float.parseFloat(json.optString("y"));
	}
	
	private String streetNumber()  {
		if (json.isNull("number")) {
			return "";
		}
		return " " + json.optString("number");
	}
	
	
}
