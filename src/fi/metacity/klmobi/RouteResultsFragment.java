package fi.metacity.klmobi;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.res.BooleanRes;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EFragment
@OptionsMenu(R.menu.routes)
public class RouteResultsFragment extends SherlockListFragment {

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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		View header = View.inflate(getSherlockActivity(), R.layout.route_results_header, null);
		header.setClickable(false);
		header.setFocusable(false);
		((TextView)header.findViewById(R.id.fromTextView)).setText(mGlobals.getStartAddress().streetOnly());
		((TextView)header.findViewById(R.id.toTextView)).setText(mGlobals.getEndAddress().streetOnly());

		ListView listView = getListView();
		listView.addHeaderView(header);

		if (mGlobals.getRoutes().isEmpty()) {
			fetchRoutes();
		} else {
			setRoutesAdapter(mGlobals.getRoutes());
		}
	}

	@Background
	public void fetchRoutes() {
		String naviciRequest = buildNaviciRequest(mGlobals.getStartAddress(), mGlobals.getEndAddress());

		Map<String, String> params = new HashMap<String, String>();
		params.put("requestXml", naviciRequest);
		try {
			String naviciResponse = Utils.httpPost(mPreferences.baseUrl().get() + "ajaxRequest.php?token=" 
					+ mGlobals.getToken(), params);
			List<Route> routes = buildRouteList(naviciResponse);
			setRoutesAdapter(routes);
		} catch (IOException ioex) {
			Log.e(TAG, ioex.toString());
			ioex.printStackTrace();
		}
	}

	@UiThread
	public void setRoutesAdapter(List<Route> routes) {
		RouteAdapter adapter = new RouteAdapter(getSherlockActivity(), routes);
		setListAdapter(adapter);
		setListShown(true);
		setRightPane(0);
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

	private List<Route> buildRouteList(String naviciResponse) {
		Elements doc = Jsoup.parse(naviciResponse, "", Parser.xmlParser()).select("MTRXML");
		mGlobals.setDetailsXmlString(doc.outerHtml());

		Elements xmlRoutes = doc.select("route");
		for (Element xmlRoute : xmlRoutes) {
			List<RouteComponent> routeComponents = new ArrayList<RouteComponent>(); 

			Element xmlLength = xmlRoute.select("length").first();
			float duration = Float.parseFloat(xmlLength.attr("time"));
			float distance = Float.parseFloat(xmlLength.attr("dist"));

			Elements xmlRouteComponents = xmlRoute.select("walk, line");
			for (Element xmlComponent : xmlRouteComponents) {

				String code = ("line".equals(xmlComponent.tagName()) ? xmlComponent.attr("code") : "W");
				Element lengthTag = xmlComponent.select("length").first();
				float componentDuration = Float.parseFloat(lengthTag.attr("time"));
				float componentDistance = Float.parseFloat(lengthTag.attr("dist"));
				String componentStartName = null;
				String componentEndName = null;
				Date componentStartDateTime = null;
				Date componentEndDateTime = null;

				Elements xmlWayPoints = xmlComponent.select("stop, maploc, point");
				List<WayPoint> wayPoints = new ArrayList<WayPoint>();
				int i = 0;
				int xmlWayPointsLen = xmlWayPoints.size();
				for (Element xmlWayPoint : xmlWayPoints) {
					if ("stop".equals(xmlWayPoint.tagName())) {
						if (i == 0) {
							// on first STOP tag, "end"/DEPARTURE-tag implies time correctly
							componentStartDateTime = dateFromStopOrPoint(xmlWayPoint, false);
							componentStartName = xmlWayPoint.select("name").first().attr("val");
						} else if (i == xmlWayPointsLen-1) {
							// on last STOP tag, "start"/ARRIVAL-tag implies time correctly
							componentEndDateTime = dateFromStopOrPoint(xmlWayPoint, true);
							componentEndName = xmlWayPoint.select("name").first().attr("val");
						}
					} else if ("point".equals(xmlWayPoint.tagName())) {
						if ("start".equals(xmlWayPoint.attr("uid"))) {
							componentStartName = mGlobals.getStartAddress().streetOnly();
							componentStartDateTime = dateFromStopOrPoint(xmlWayPoint, true); 
						} else {
							componentEndName = mGlobals.getEndAddress().streetOnly();
							componentEndDateTime = dateFromStopOrPoint(xmlWayPoint, false); 
						}
					}

					Element nameTag = xmlWayPoint.select("name").first();
					String time = xmlWayPoint.select("departure").first().attr("time");
					wayPoints.add(new WayPoint(xmlWayPoint.attr("x"), xmlWayPoint.attr("y"), 
							(nameTag != null) ? nameTag.attr("val") : "", time));

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
			mGlobals.getRoutes().add(new Route(routeComponents, duration, distance)); 
		}

		return mGlobals.getRoutes();
	}

	private static Date dateFromStopOrPoint(Element element, boolean start) {
		String date;
		String time;
		Element timeOfInterest = element.select(start ? "arrival" : "departure").first();

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

		SimpleDateFormat dateSdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
		mDate = dateSdf.format(newDateTime);

		SimpleDateFormat timeSdf = new SimpleDateFormat("HHmm", Locale.US);
		mTime = timeSdf.format(newDateTime);

		mGlobals.getRoutes().clear();
		fetchRoutes();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mIsDualPane) {			
			setRightPane(position-1); // Header is 0!
		}
	}
	
	private void setRightPane(int position) {
		if (mIsDualPane) {			
			PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getSherlockActivity().findViewById(R.id.tabs);
			ViewPager pager = (ViewPager) getSherlockActivity().findViewById(R.id.pager);
			if (tabs != null && pager != null) {
				DetailsPagerAdapter adapter = new DetailsPagerAdapter(getFragmentManager(), 
						new String[] { "Reittitiedot", "Vaihtokuvat", "Kartta" }, position); 
				pager.setAdapter(adapter);
				tabs.setViewPager(pager);
				pager.setCurrentItem(0, true);
			}
		}
	}
}
