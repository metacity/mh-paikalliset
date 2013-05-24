package fi.metacity.klmobi;

import org.json.*;

public class Address {
	final JSONObject jsonAddress;
	
	public Address(JSONObject jsonAddress) {
		this.jsonAddress = jsonAddress;
	}
	
	public String toString() {
		return jsonAddress.optString("name") 
				+ streetNumber() 
				+ ", " + jsonAddress.optString("city");
	}

	String streetOnly() {
		return jsonAddress.optString("name") + streetNumber(); 
	}
	
	private String streetNumber()  {
		if (jsonAddress.has("number"))
			return " " + jsonAddress.optString("number");
		return "";
	}
}
