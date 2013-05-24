package fi.metacity.klmobi;

import java.io.IOException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

public class LocationToFocusListener implements LocationListener {
	private static final int MAX_FIX_AGE = 2 * 60 * 1000; // 2 minutes

	private final LocationManager mLocationManager;
	private final Geocoder mGeocoder;
	private final EditText mTargetView;
	private final int mTargetAccuracy;
	private final ProgressDialog pd;
	private final String mProvider;
	private boolean mStopLocating = false;
	private Location mLatestLocation;

	public LocationToFocusListener(
			LocationManager locationManager,
			Geocoder geocoder,
			EditText targetView, 
			int targetAccuracy, 
			int timeOut, 
			String provider) {

		mLocationManager = locationManager;
		mGeocoder = geocoder;
		mTargetView = targetView;
		mTargetAccuracy = targetAccuracy;
		Context context = targetView.getContext();
		mProvider = provider;;
		pd = ProgressDialog.show(context, 
				context.getResources().getString(R.string.progressDialogTitle) + " (max. " + timeOut + " sec)",
				context.getResources().getString(R.string.progressDialogLocating) + " (" + provider + ")",
				true, 
				true,
				new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mLocationManager.removeUpdates(LocationToFocusListener.this);
			}
		}
				);

		// Cancel requesting for location after specified amount of time
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (pd.isShowing() && mLatestLocation != null) { // show only if dialog is still open (= locating in progress)
					mStopLocating = true;
					onLocationChanged(mLatestLocation);
				}
			}
		}, timeOut * 1000);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (System.currentTimeMillis() - location.getTime() > MAX_FIX_AGE) // completely ignore locations acquired ages ago
			return;

		if (mLatestLocation != null && mLatestLocation.getAccuracy() < location.getAccuracy())
			return;

		mLatestLocation = location;

		String locationMsg = pd.getContext().getResources().getString(R.string.progressDialogLocating);
		String accuracy = pd.getContext().getResources().getString(R.string.accuracy);
		pd.setMessage(locationMsg + " (" + mProvider + ")\n\n" + accuracy + ": " + (int)location.getAccuracy() + " m");
		if ((int)location.getAccuracy() < mTargetAccuracy || mStopLocating) {
			mLocationManager.removeUpdates(this);
			// fetch street etc and place to targetView
			try { 
				List<Address> addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				mTargetView.setText(addresses.get(0).getAddressLine(0));
				((AutoCompleteTextView) mTargetView).showDropDown();
			} catch (IOException ioex) {
				// LOL
			}
			pd.dismiss();
			Toast.makeText(pd.getContext(), accuracy + ": " + (int)location.getAccuracy() + " m", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Ignore
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Ignore
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Ignore
	}

}
