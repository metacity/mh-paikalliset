package fi.metacity.klmobi;

import org.json.JSONObject;

public class Address {
	final JSONObject json;
	
	public Address(JSONObject json) {
		this.json = json;
	}
	
	public String toString() {
		return json.optString("name") 
				+ streetNumber() 
				+ ", " + json.optString("city");
	}

	String streetOnly() {
		return json.optString("name") + streetNumber(); 
	}
	
	private String streetNumber()  {
		if (json.has("number"))
			return " " + json.optString("number");
		return "";
	}
}
