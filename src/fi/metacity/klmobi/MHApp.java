package fi.metacity.klmobi;

import com.googlecode.androidannotations.annotations.EApplication;

import android.app.Application;

@EApplication
public class MHApp extends Application {
	
	private String mToken = "";
	private Address mStartAddress;
	private Address mEndAddress;
	
	public String getToken() {
		return mToken;
	}
	
	public void setToken(String token) {
		mToken = token;
	}
	
	public Address getStartAddress() {
		return mStartAddress;
	}
	
	public void setStartAddress(Address address) {
		mStartAddress = address;
	}
	
	public Address getEndAddress() {
		return mEndAddress;
	}
	
	public void setEndAddress(Address address) {
		mEndAddress = address;
	}
}
