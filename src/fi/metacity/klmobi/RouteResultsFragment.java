package fi.metacity.klmobi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.util.Log;

import com.actionbarsherlock.app.SherlockListFragment;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.res.BooleanRes;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EFragment
public class RouteResultsFragment extends SherlockListFragment {
	private static final String TAG = "RouteResultsFragment";

	@App
	MHApp mGlobals;
	
	@Pref
	Preferences_ mPreferences;

	@BooleanRes(R.bool.has_two_panes)
	boolean isDualPane;
	
	@FragmentArg(Constants.EXTRA_DATE)
	String mDate;
	
	@FragmentArg(Constants.EXTRA_TIME)
	String mTime;
	
	@FragmentArg(Constants.EXTRA_NUMBER_OF_ROUTES)
	String mNumerOfRoutes;
	
	@FragmentArg(Constants.EXTRA_ROUTING_TYPE)
	String mRoutingType;
	
	@FragmentArg(Constants.EXTRA_WALKING_SPEED)
	String mWalkingSpeed;
	
	@FragmentArg(Constants.EXTRA_MAX_WALKING_DISTANCE)
	String mMaxWalkingDistance;
	
	@FragmentArg(Constants.EXTRA_CHANGE_MARGIN)
	String mChangeMargin;
	
	ProgressDialog mProgressDialog;

	@Override
	public void onStart() {
		super.onStart();
		mProgressDialog = ProgressDialog.show(
				getActivity(), 
				getString(R.string.progressDialogTitle), 
				getString(R.string.progressDialogLoadingRoutes), 
				true, 
				true
		);
		fetchRoutes();
	}
	
	@Background
	public void fetchRoutes() {
		String naviciRequest = buildNaviciRequest(mGlobals.getStartAddress(), mGlobals.getEndAddress());
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("requestXml", naviciRequest);
		try {
			String response = HttpUtils.post(mPreferences.baseUrl().get() + "ajaxRequest.php?token=" 
					+ mGlobals.getToken(), params);
			Log.i(TAG, response);
		} catch (IOException ioex) {
			Log.e(TAG, ioex.toString());
		}
	}
	
	@UiThread
	public void setRoutesAdapter(List<Route> routes) {
		
	}
	
	private String buildNaviciRequest(Address start, Address end) {
		String startX = start.json.optString("x");
		String startY = start.json.optString("y");
		String startName = start.json.optString("name");
		String startNumber = start.json.optString("number"); 
		String startCity = start.json.optString("city");
		
		String endX = end.json.optString("x");
		String endY = end.json.optString("y");
		String endName = end.json.optString("name");
		String endNumber = end.json.optString("number"); 
		String endCity = end.json.optString("city");
		
		String naviciRequest = 
				"<navici_request>"
					+ "<ajax_request_object object_id=\"1\" service=\"RouteRequests\">"
						+ "<get_route id=\"1\" language=\"fi\" TimeDirection=\"forward\" Date=\"" + mDate 
						+ "\" Time=\"" + mTime + "\" WalkSpeed=\"" + mWalkingSpeed + "\" MaxWalk=\"" 
						+ mMaxWalkingDistance + "\" RoutingMethod=\"" + mRoutingType + "\" ChangeMargin=\"" 
						+ mChangeMargin + "\" NumberRoutes=\"" + mNumerOfRoutes + "\" ExcludedLines=\"\" >"
							+ "<output type=\"image_layer_objects\"/>"
							+ "<output type=\"gui_objects\"/>"
							+ "<location order=\"0\" x=\"" + startX + "\" y=\"" + startY + "\" name=\"" 
							+ startName + "\" number=\"" + startNumber + "\" city=\"" + startCity + "\" />"
							+ "<location order=\"1\" x=\"" + endX + "\" y=\"" + endY + "\" name=\"" + endName 
							+ "\" number=\"" + endNumber + "\" city=\"" + endCity + "\" />"
						+ "</get_route>"
					+ "</ajax_request_object>"
				+ "</navici_request>";
		
		return naviciRequest;
	}
}
