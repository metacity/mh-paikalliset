package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.EApplication;

import android.app.Application;

@EApplication
public class MHApp extends Application {
	
	private final Object mLock = new Object();
	
	private Address mStartAddress;
	private Address mEndAddress;
	
	private List<Route> mRoutes = new ArrayList<Route>();
	
	private String mDetailsXmlRequest = "";
	private String mTurkuMapQueryString = "";
	
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
	
	public void setTurkuMapQueryString(String queryString) {
		synchronized (mLock) {
			mTurkuMapQueryString = queryString;
		}
	}
	
	public String getTurkuMapQueryString() {
		synchronized (mLock) {
			return mTurkuMapQueryString;
		}
	}
}
