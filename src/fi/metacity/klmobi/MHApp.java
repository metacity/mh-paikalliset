package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.googlecode.androidannotations.annotations.EApplication;

@EApplication
public class MHApp extends Application {
	
	private String mToken = "";
	private Address mStartAddress;
	private Address mEndAddress;
	
	private List<Route> mRoutes = new ArrayList<Route>();
	private String mDetailsXmlRequest = "";
	
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
	
	public List<Route> getRoutes() {
		return mRoutes;
	}
	
	public String getDetailsXmlRequest() {
		return mDetailsXmlRequest;
	}
	
	public void setDetailsXmlString(String xmlRequest) {
		mDetailsXmlRequest = xmlRequest;
	}
}
