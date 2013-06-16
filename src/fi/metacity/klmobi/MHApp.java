package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.googlecode.androidannotations.annotations.EApplication;

@EApplication
public class MHApp extends Application {
	
	private final Object mLock = new Object();
	
	private String mToken = "";
	private Address mStartAddress;
	private Address mEndAddress;
	
	private List<Route> mRoutes = new ArrayList<Route>();
	private String mDetailsXmlRequest = "";
	
	public String getToken() {
		synchronized (mLock) {
			return mToken;
		}
	}
	
	public void setToken(String token) {
		synchronized (mLock) {
			mToken = token;
		}
	}
	
	public Address getStartAddress() {
		synchronized (mLock) {
			return mStartAddress;
		}
		
	}
	
	public void setStartAddress(Address address) {
		synchronized (mLock) {
			mStartAddress = address;
		}
	}
	
	public Address getEndAddress() {
		synchronized (mLock) {
			return mEndAddress;
		}
	}
	
	public void setEndAddress(Address address) {
		synchronized (mLock) {
			mEndAddress = address;
		}
	}
	
	public List<Route> getRoutes() {
		synchronized (mLock) {
			return mRoutes;
		}
	}
	
	public void setRoutes(List<Route> routes) {
		synchronized (mLock) {
			mRoutes = routes;
		}
	}
	
	public String getDetailsXmlRequest() {
		synchronized (mLock) {
			return mDetailsXmlRequest;
		}
	}
	
	public void setDetailsXmlString(String xmlRequest) {
		synchronized (mLock) {
			mDetailsXmlRequest = xmlRequest;
		}
	}
}
