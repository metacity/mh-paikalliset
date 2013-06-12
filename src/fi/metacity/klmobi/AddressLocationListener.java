package fi.metacity.klmobi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class AddressLocationListener implements LocationListener {
	private static final int MAX_FIX_AGE = 2 * 60 * 1000; // 2 minutes

	private final Context mContext;
	private final LocationManager mLocationManager;
	private final int mTargetAccuracy;
	private final String mProvider;
	private final OnLocationFoundListener mFoundListener;
	
	private final ProgressDialog pd;
	private boolean mStopLocating = false;
	private Location mLatestLocation;

	public AddressLocationListener(
			Context context,
			int targetAccuracy, 
			int timeOut, 
			String provider,
			OnLocationFoundListener foundListener) {

		mContext = context;
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mTargetAccuracy = targetAccuracy;
		mProvider = provider;
		mFoundListener = foundListener;
		
		pd = ProgressDialog.show(context, 
				context.getString(R.string.progressDialogTitle) + " (max. " + timeOut + " sec)",
				context.getString(R.string.progressDialogLocating) + " (" + provider + ")",
				true, 
				true,
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mLocationManager.removeUpdates(AddressLocationListener.this);
					}
				}
		);

		// Cancel requesting for location after specified amount of time
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (pd.isShowing()) { // Dismiss only if dialog is still open (= locating in progress)
					mStopLocating = true;
					mLocationManager.removeUpdates(AddressLocationListener.this);
					pd.dismiss();
					if (mLatestLocation == null) {
						Toast.makeText(mContext, mContext.getString(R.string.failedToObtainLocation), 
								Toast.LENGTH_LONG).show();
					} else {
						onLocationChanged(mLatestLocation);
					}
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

		String locationMsg = mContext.getString(R.string.progressDialogLocating);
		String accuracy = mContext.getString(R.string.accuracy);
		pd.setMessage(locationMsg + " (" + mProvider + ")\n\n" + accuracy + ": " + (int)location.getAccuracy() + " m");
		if ((int)location.getAccuracy() <= mTargetAccuracy || mStopLocating) {
			mLocationManager.removeUpdates(this);
			mFoundListener.onLocationFound(location);
			pd.dismiss();
			Toast.makeText(mContext, accuracy + ": " + (int)location.getAccuracy() + " m", Toast.LENGTH_LONG).show();
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
	
	public static interface OnLocationFoundListener {
		void onLocationFound(Location location);
	}

}
