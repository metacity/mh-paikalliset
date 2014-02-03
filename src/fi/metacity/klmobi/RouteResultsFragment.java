package fi.metacity.klmobi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.BooleanRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

@EFragment
@OptionsMenu(R.menu.routes)
public class RouteResultsFragment extends ListFragment {

	private static final String TAG = "RouteResultsFragment";

	@App
	MHApp mGlobals;

	@Pref
	Preferences_ mPreferences;
	
	@BooleanRes(R.bool.has_two_panes)
	boolean mIsDualPane;

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
	
	@FragmentArg(Constants.EXTRA_TIME_DIRECTION)
	String mTimeDirection;
	
	@FragmentArg(Constants.EXTRA_ROUTE_INDEX)
	int mInitialRouteIndex;
	
	private ViewPager mPager;
	private PagerSlidingTabStrip mTabs;
	private Button mShowInMapBtn;

	private RouteAdapter mAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (mGlobals.getStartAddress() == null) {
			getActivity().finish();
			return;
		}
		
		mPager = (ViewPager) getActivity().findViewById(R.id.pager);
		mTabs = (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs);
		mShowInMapBtn = (Button) getActivity().findViewById(R.id.showInMapBtn);

		View header = View.inflate(getActivity(), R.layout.route_results_header, null);
		header.setClickable(false);
		header.setFocusable(false);
		header.setBackgroundResource(R.color.background);
		((TextView)header.findViewById(R.id.fromTextView)).setText(mGlobals.getStartAddress().shortName());
		((TextView)header.findViewById(R.id.toTextView)).setText(mGlobals.getEndAddress().shortName());

		ListView listView = getListView();
		listView.addHeaderView(header);
		listView.setBackgroundResource(R.color.background);
		listView.setDivider(getResources().getDrawable(android.R.color.transparent));

		if (mGlobals.getRoutes().isEmpty()) {
			fetchRoutes();
		} else {
			setRoutesAdapter(mGlobals.getRoutes());
		}
	}

	@Background
	public void fetchRoutes() {
		Map<String, String> params = new HashMap<String, String>();
		String url = "";
		
		if (mPreferences.selectedCityIndex().get() == Constants.TURKU_INDEX) {
			url = Constants.TURKU_BASE_URL + "getroute.php";
			putTurkuPostParams(params, mGlobals.getStartAddress(), mGlobals.getEndAddress());
		} else {
			url = mPreferences.baseUrl().get() + "ajaxRequest.php?token=" + mPreferences.token().get();
			String naviciRequest = buildNaviciRequest(mGlobals.getStartAddress(), mGlobals.getEndAddress());
			params.put("requestXml", naviciRequest);
		}
		
		try {
			String naviciResponse = Utils.httpPost(url, params);
			List<Route> routes = buildRouteList(naviciResponse);
			setRoutesAdapter(routes);
		} catch (IOException ioex) {
			Log.e(TAG, ioex.toString());
			showNetworkErrorDialog();
		}
	}

	@UiThread
	public void setRoutesAdapter(List<Route> routes) {
		mGlobals.setRoutes(routes);
		mAdapter = new RouteAdapter(getActivity(), routes);
		setListAdapter(mAdapter);
		setListShown(true);
		
		if (mIsDualPane) {
			setRightPane(mInitialRouteIndex);
			setSelectionIndication(getListView(), mInitialRouteIndex);
		} else { // Clear existing selection indications
			for (Route route : mGlobals.getRoutes()) {
				route.isSelected = false;
			}
			mAdapter.notifyDataSetChanged();
		}
		
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
						+ "<get_route id=\"1\" language=\"fi\" TimeDirection=\"" + mTimeDirection + "\" Date=\"" + mDate 
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
	
	private void putTurkuPostParams(Map<String, String> targetParams, Address start, Address end) {
		targetParams.put("request[changeMargin]", mChangeMargin);
		targetParams.put("request[walkSpeed]", mWalkingSpeed);
		targetParams.put("request[maxTotWalkDist]", mMaxWalkingDistance);
		targetParams.put("request[timeDirection]", mTimeDirection);
		targetParams.put("request[numberRoutes]", mNumerOfRoutes);
		targetParams.put("request[routingMethod]", mRoutingType);
		targetParams.put("request[start][x]", start.json.optString("x"));
		targetParams.put("request[start][y]", start.json.optString("y"));
		targetParams.put("request[end][x]", end.json.optString("x"));
		targetParams.put("request[end][y]", end.json.optString("y"));
		targetParams.put("token", mPreferences.token().get());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
		String timestamp = "";
		try {
			timestamp = String.valueOf(sdf.parse(mDate + mTime).getTime()/1000);
	        targetParams.put("request[timestamp]", timestamp);
        } catch (ParseException e) {
	        // Ignore
        }
		
		targetParams.put("request[via]", "null");
		targetParams.put("request[excludedLines]", "null");
		targetParams.put("request[includedLines]", "null");
		
		// Also build the GET query string for transfer images
		try {
			String mapQueryString = "startlocation=" + URLEncoder.encode(mGlobals.getStartAddress().shortName(), "UTF-8") +
					"&endlocation=" + URLEncoder.encode(mGlobals.getEndAddress().shortName(), "UTF-8") + 
					"&changeMargin=" + mChangeMargin + "&walkSpeed=" + mWalkingSpeed + 
					"&maxTotWalkDist=" + mMaxWalkingDistance + "&timeDirection=" + mTimeDirection + 
					"&numberRoutes=" + mNumerOfRoutes + "&timestamp=" + timestamp +
					"&routingMethod=" + mRoutingType + "&start[x]=" + start.json.optString("x") +
					"&start[y]=" + start.json.optString("y") + "&end[x]=" + end.json.optString("x") +
					"&end[y]=" + end.json.optString("y") + "&via=null" + "&excludedLines=null" + 
					"&includedLines=null";
			mGlobals.setTurkuMapQueryString(mapQueryString);
		} catch (UnsupportedEncodingException useex) {
			// Ignore
		}
		
	}

	private List<Route> buildRouteList(String naviciResponse) {
		List<Route> routes = new ArrayList<Route>();
		Document fullDoc = Jsoup.parse(naviciResponse, "", Parser.xmlParser());
		fullDoc.outputSettings().charset("UTF-8").indentAmount(0).escapeMode(EscapeMode.xhtml);
		Elements doc = fullDoc.select("MTRXML");
		if (mPreferences.selectedCityIndex().get() != Constants.TURKU_INDEX) {
			mGlobals.setDetailsXmlString(doc.toString());
		}

		Elements xmlRoutes = doc.select("ROUTE");
		for (Element xmlRoute : xmlRoutes) {
			List<RouteComponent> routeComponents = new ArrayList<RouteComponent>(); 

			Element xmlLength = xmlRoute.select("LENGTH").first();
			float duration = Float.parseFloat(xmlLength.attr("time"));
			float distance = Float.parseFloat(xmlLength.attr("dist"));

			Elements xmlRouteComponents = xmlRoute.select("WALK, LINE");
			for (Element xmlComponent : xmlRouteComponents) {

				String code = ("LINE".equals(xmlComponent.tagName()) ? xmlComponent.attr("code") : "W");
				Element lengthTag = xmlComponent.select("LENGTH").first();
				float componentDuration = Float.parseFloat(lengthTag.attr("time"));
				float componentDistance = Float.parseFloat(lengthTag.attr("dist"));
				String componentStartName = null;
				String componentEndName = null;
				Date componentStartDateTime = null;
				Date componentEndDateTime = null;

				Elements xmlWayPoints = xmlComponent.select("STOP, MAPLOC, POINT");
				List<WayPoint> wayPoints = new ArrayList<WayPoint>();
				int i = 0;
				int xmlWayPointsLen = xmlWayPoints.size();
				for (Element xmlWayPoint : xmlWayPoints) {
					if ("STOP".equals(xmlWayPoint.tagName())) {
						if (i == 0) {
							// on first STOP tag, "end"/DEPARTURE-tag implies time correctly
							componentStartDateTime = dateFromStopOrPoint(xmlWayPoint, false);
							componentStartName = xmlWayPoint.select("NAME").first().attr("val");
						} else if (i == xmlWayPointsLen-1) {
							// on last STOP tag, "start"/ARRIVAL-tag implies time correctly
							componentEndDateTime = dateFromStopOrPoint(xmlWayPoint, true);
							componentEndName = xmlWayPoint.select("NAME").first().attr("val");
						}
					} else if ("POINT".equals(xmlWayPoint.tagName())) {
						if ("start".equals(xmlWayPoint.attr("uid"))) {
							componentStartName = mGlobals.getStartAddress().shortName();
							componentStartDateTime = dateFromStopOrPoint(xmlWayPoint, true); 
						} else {
							componentEndName = mGlobals.getEndAddress().shortName();
							componentEndDateTime = dateFromStopOrPoint(xmlWayPoint, false); 
						}
					}

					Element nameTag = xmlWayPoint.select("NAME").first();
					Elements digistopIdTags = xmlWayPoint.select("XTRA[name=digistop_id]");
					String time = "";
					if ("WALK".equals(xmlComponent.tagName()) && "STOP".equals(xmlWayPoint.tagName())) {
						time = xmlWayPoint.select("ARRIVAL").first().attr("time");
					} else {
						time = xmlWayPoint.select("DEPARTURE").first().attr("time");
					}
					
					String wayPointName = (nameTag != null) ? nameTag.attr("val") : "";
					if (digistopIdTags != null && digistopIdTags.size() > 0) {
						wayPointName += " (" + digistopIdTags.first().attr("val") + ")";
					}
					wayPoints.add(new WayPoint(xmlWayPoint.attr("x"), xmlWayPoint.attr("y"), 
							wayPointName, time));

					++i;
				}

				routeComponents.add(
						new RouteComponent(
								code, 
								componentStartName, 
								componentEndName,
								componentStartDateTime, 
								componentEndDateTime, 
								componentDuration, 
								componentDistance, 
								wayPoints)
						);
			}
			routes.add(new Route(routeComponents, duration, distance)); 
		}

		return routes;
	}

	private static Date dateFromStopOrPoint(Element element, boolean start) {
		String date;
		String time;
		Element timeOfInterest = element.select(start ? "ARRIVAL" : "DEPARTURE").first();

		date = timeOfInterest.attr("date");
		time = timeOfInterest.attr("time");

		try {
			return Utils.dateTimeFormat.parse(date + ";" + time);
		} catch (ParseException pex) {
			return new Date();
		}
	}

	@OptionsItem({R.id.earlier_lines, R.id.later_lines})
	public void showEarlierOrLaterLines(MenuItem item) {
		if (mGlobals.getRoutes().size() == 0)
			return;
		
		setListShown(false);
		setListAdapter(null);

		List<Route> routes = mGlobals.getRoutes();
		Route lastRoute = routes.get(routes.size() - 1);
		Date lastStart = lastRoute.routeComponents.get(0).startDateTime;

		Date newDateTime;
		if (item.getItemId() == R.id.earlier_lines) {
			Route firstRoute = routes.get(0);
			Date firstStart = firstRoute.routeComponents.get(0).startDateTime;

			String currentDateTimeStr = mDate + ";" + mTime;
			Date currentDate = null;
			try {
				currentDate = Utils.dateTimeFormat.parse(currentDateTimeStr);
			} catch (ParseException pex) {
				// Ignore
			}

			// Difference between the first and the last of current routes
			long delta = lastStart.getTime() - firstStart.getTime();
			newDateTime = new Date(currentDate.getTime() - delta);
		} else {
			newDateTime = new Date(lastStart.getTime() + 60 * 1000); // + 1 minute
		}

		SimpleDateFormat dateSdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
		mDate = dateSdf.format(newDateTime);

		SimpleDateFormat timeSdf = new SimpleDateFormat("HHmm", Locale.ENGLISH);
		mTime = timeSdf.format(newDateTime);

		mGlobals.getRoutes().clear();
		fetchRoutes();
	}
	
	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		super.onListItemClick(list, v, position, id);
		if (position > 0) { // Ignore header click
			if (mIsDualPane) {  // SHOW RIGHT PANE
				setSelectionIndication(list,  position-1);
				setRightPane(position-1); // Header is 0!
			} else { // START DETAILS ACTIVITY
				RouteDetailsActivity_.intent(getActivity()).mRouteIndex(position-1).start();
			}
		}
	}
	
	// Header excluded here already!
	private void setRightPane(int position) {
		if (mTabs != null && mPager != null) {
			RouteDetailsPagerAdapter adapter = new RouteDetailsPagerAdapter(
					getFragmentManager(), 
					new String[] { 
						getString(R.string.routeDetailsTitle), 
						getString(R.string.transferImages)
					}, 
					position
			);
			
			mPager.setAdapter(adapter);
			mTabs.setViewPager(mPager);
			mPager.setCurrentItem(0, true);
			if (mShowInMapBtn != null) mShowInMapBtn.setVisibility(View.VISIBLE);
		}
	}
	
	// Header excluded here already!
	private void setSelectionIndication(ListView list, int position) {
		if (position < mGlobals.getRoutes().size()) {
			for (Route route : mGlobals.getRoutes()) {
				route.isSelected = false;
			}
			mGlobals.getRoutes().get(position).isSelected = true;
			mAdapter.notifyDataSetChanged();
		}
	}
	
	@UiThread
	public void showNetworkErrorDialog() {
		new AlertDialog.Builder(getActivity())
			.setMessage(R.string.networkErrorOccurred).setPositiveButton("OK", null).show();
		setListShown(true);
	}
}
