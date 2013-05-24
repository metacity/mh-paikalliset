package fi.metacity.klmobi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import android.R.color;
import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class RouteResultsActivity extends ListActivity {
	
	private AQuery aq;
	private Bundle mConfigData;
	
	private int mSelectedCityIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (GlobalApp.startAddress == null 
		 || GlobalApp.endAddress == null) {
			finish();
			return;
		}
		
		aq = new AQuery(this);
		aq.hardwareAccelerated11();
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		View header = getLayoutInflater().inflate(R.layout.route_results_header, null);
		header.setClickable(false);
		header.setFocusable(false);
		TextView fromTextView = (TextView) header.findViewById(R.id.fromTextView);
		TextView toTextView = (TextView) header.findViewById(R.id.toTextView);
		fromTextView.setText(GlobalApp.startAddress.streetOnly());
		toTextView.setText(GlobalApp.endAddress.streetOnly());
		ListView lv = getListView();
		lv.addHeaderView(header);
		lv.setBackgroundColor(color.background_light);
		lv.setCacheColorHint(color.background_light);
		
		// Get config data bundle
		mConfigData = getIntent().getExtras();
		mSelectedCityIndex = mConfigData.getInt(Constants.EXTRA_CITY_INDEX);
		
		// Fetch and display routes
		fetchRoutes();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_route_results, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
				
			case R.id.later_lines: case R.id.earlier_lines: 
				Route lastRoute = GlobalApp.routes.get(GlobalApp.routes.size() - 1);
				Date lastStart = lastRoute.routeComponents.get(0).startDateTime;
				
				Date newDateTime;
				if (item.getItemId() == R.id.earlier_lines) {
					Route firstRoute = GlobalApp.routes.get(0);
					Date firstStart = firstRoute.routeComponents.get(0).startDateTime;
					
					String currentDateTimeStr = mConfigData.getString(Constants.EXTRA_DATE)
							+ ";" + mConfigData.getString(Constants.EXTRA_TIME);
					Date currentDate = null;
					try {
						currentDate = KlmobiUtils.datetime_format.parse(currentDateTimeStr);
					} catch (ParseException pex) {
						break;
					}
					
					// difference between the first and the last of current routes
					long delta = lastStart.getTime() - firstStart.getTime();
					newDateTime = new Date(currentDate.getTime() - delta);
				} else {
					newDateTime = new Date(lastStart.getTime() + 60 * 1000); // + 1 minute
				}
				
				SimpleDateFormat dateSdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
				String newDate = dateSdf.format(newDateTime);
				mConfigData.putString(Constants.EXTRA_DATE, newDate);
				
				SimpleDateFormat timeSdf = new SimpleDateFormat("HHmm", Locale.US);
				String newTime = timeSdf.format(newDateTime);
				mConfigData.putString(Constants.EXTRA_TIME, newTime);
				
				// Fetch routes with new date and time
				fetchRoutes();
				
				return true;

		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		if (position > 0) {
			Intent intent = new Intent(this, RouteDetailsActivity.class);
	
			intent.putExtra(Constants.EXTRA_ROUTE_INDEX, position-1); // header is index 0, hence -1 !
			intent.putExtra(Constants.EXTRA_CITY_INDEX, mSelectedCityIndex);
			startActivity(intent);
		}
	}
	
	private void fetchRoutes() {
		// Show progress dialog..
		final ProgressDialog pd = ProgressDialog.show(this, 
				this.getResources().getString(R.string.progressDialogTitle), 
				this.getResources().getString(R.string.progressDialogLoadingRoutes), true, true);
		
		// Get config messages
		String date = mConfigData.getString(Constants.EXTRA_DATE);
		String time = mConfigData.getString(Constants.EXTRA_TIME);
		String numberOfRoutes = mConfigData.getString(Constants.EXTRA_NUMBER_OF_ROUTES);
		String routeType = mConfigData.getString(Constants.EXTRA_ROUTE_TYPE);
		String walkingSpeed = mConfigData.getString(Constants.EXTRA_WALKING_SPEED);
		String maxWalkingDistance = mConfigData.getString(Constants.EXTRA_MAX_WALKING_DISTANCE);
		String changeMargin = mConfigData.getString(Constants.EXTRA_CHANGE_MARGIN);
		
		// Get start and end point data
		Address start = GlobalApp.startAddress;
		Address end = GlobalApp.endAddress;

		String startX = start.jsonAddress.optString("x");
		String startY = start.jsonAddress.optString("y");
		String startName = start.jsonAddress.optString("name");
		String startNumber = start.jsonAddress.optString("number"); 
		String startCity = start.jsonAddress.optString("city");
		
		String endX = end.jsonAddress.optString("x");
		String endY = end.jsonAddress.optString("y");
		String endName = end.jsonAddress.optString("name");
		String endNumber = end.jsonAddress.optString("number"); 
		String endCity = end.jsonAddress.optString("city");
		
		String naviciRequest = 
				"<navici_request>"
					+ "<ajax_request_object object_id=\"1\" service=\"RouteRequests\">"
						+ "<get_route id=\"1\" language=\"fi\" TimeDirection=\"forward\" Date=\"" + date + "\" Time=\"" + time + "\" WalkSpeed=\"" + walkingSpeed + "\" MaxWalk=\"" + maxWalkingDistance + "\" RoutingMethod=\"" + routeType + "\" ChangeMargin=\"" + changeMargin + "\" NumberRoutes=\"" + numberOfRoutes + "\" ExcludedLines=\"\" >"
							+ "<output type=\"image_layer_objects\"/>"
							+ "<output type=\"gui_objects\"/>"
							+ "<location order=\"0\" x=\"" + startX + "\" y=\"" + startY + "\" name=\"" + startName + "\" number=\"" + startNumber + "\" city=\"" + startCity + "\" />"
							+ "<location order=\"1\" x=\"" + endX + "\" y=\"" + endY + "\" name=\"" + endName + "\" number=\"" + endNumber + "\" city=\"" + endCity + "\" />"
						+ "</get_route>"
					+ "</ajax_request_object>"
				+ "</navici_request>";
		
		String url = "http://" + Constants.citySubdomains[mSelectedCityIndex] + ".matkahuolto.info/ajaxRequest.php?token=" + GlobalApp.token;
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("requestXml", naviciRequest);
		
		aq.ajax(url, params, XmlDom.class, new AjaxCallback<XmlDom>() {

			@Override
			public void callback(String url, XmlDom xml, AjaxStatus status) {
				GlobalApp.detailsXmlRequest = xml.tag("MTRXML").toString();
				Element doc = Jsoup.parse(GlobalApp.detailsXmlRequest, "", Parser.xmlParser());
				GlobalApp.routes = new ArrayList<Route>();
				
				Elements xmlRoutes = doc.select("route");
				for (Element xmlRoute : xmlRoutes) {
					List<RouteComponent> routeComponents = new ArrayList<RouteComponent>(); 
					
					Element xmlLength = xmlRoute.select("length").first();
					double duration = Double.parseDouble(xmlLength.attr("time"));
					double distance = Double.parseDouble(xmlLength.attr("dist"));

					Elements xmlRouteComponents = xmlRoute.select("walk, line");
					for (Element xmlComponent : xmlRouteComponents) {
						
						String code = ("line".equals(xmlComponent.tagName()) ? xmlComponent.attr("code") : "W");
						Element lengthTag = xmlComponent.select("length").first();
						double componentDuration = Double.parseDouble(lengthTag.attr("time"));
						double componentDistance = Double.parseDouble(lengthTag.attr("dist"));
						String componentStartName = null, componentEndName = null;
						Date componentStartDateTime = null, componentEndDateTime = null;

						Elements xmlWayPoints = xmlComponent.select("stop, maploc, point");
						List<WayPoint> wayPoints = new ArrayList<WayPoint>();
						int i = 0, xmlWayPointsLen = xmlWayPoints.size();
						for (Element xmlWayPoint : xmlWayPoints) {
							if ("stop".equals(xmlWayPoint.tagName())) {
								if (i == 0) {
									componentStartDateTime = dateFromStopOrPoint(xmlWayPoint, "end");	//  on STOP-tags, "end"/DEPARTURE-tag implies time correctly
									componentStartName = xmlWayPoint.select("name").first().attr("val");
								} else if (i == xmlWayPointsLen-1) {
									componentEndDateTime = dateFromStopOrPoint(xmlWayPoint, "start");	//  on STOP-tags, "start"/ARRIVAL-tag implies time correctly
									componentEndName = xmlWayPoint.select("name").first().attr("val");
								}
							} else if ("point".equals(xmlWayPoint.tagName())) {
								if ("start".equals(xmlWayPoint.attr("uid"))) {
									componentStartName = GlobalApp.startAddress.streetOnly();
									componentStartDateTime = dateFromStopOrPoint(xmlWayPoint, "start"); 
								} else {
									componentEndName = GlobalApp.endAddress.streetOnly();
									componentEndDateTime = dateFromStopOrPoint(xmlWayPoint, "end"); 
								}
							}
							
							Element nameTag = xmlWayPoint.select("name").first();
							String time = xmlWayPoint.select("departure").first().attr("time");
							wayPoints.add(new WayPoint(xmlWayPoint.attr("x"), xmlWayPoint.attr("y"), (nameTag != null) ? nameTag.attr("val") : "", time));

							++i;
						}
						
						routeComponents.add(new RouteComponent(code, componentStartName, componentEndName,
								componentStartDateTime, componentEndDateTime, componentDuration, componentDistance, wayPoints));
					}
					
					GlobalApp.routes.add(new Route(routeComponents, duration, distance)); 
				}

				setListAdapter(new RouteAdapter(RouteResultsActivity.this, GlobalApp.routes));
				pd.dismiss();
			}
		});
		
	}
	
	private static Date dateFromStopOrPoint(Element element, String startOrEnd) {
		String date, time;
		Element timeOfInterest;
		if ("start".equals(startOrEnd))
			timeOfInterest = element.select("arrival").first();
		else
			timeOfInterest = element.select("departure").first();

		date = timeOfInterest.attr("date");
		time = timeOfInterest.attr("time");
		
		try {
			return KlmobiUtils.datetime_format.parse(date + ";" + time);
		} catch (ParseException pex) {
			return new Date();
		}
	}
}
