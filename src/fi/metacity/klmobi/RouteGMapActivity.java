package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.Bundle;
import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

import fi.sandman.utils.coordinate.*;

public class RouteGMapActivity extends FragmentActivity {
	private GoogleMap gMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_gmap);
		
		if (GlobalApp.routes == null) {
			finish();
			return;
		}
		
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		setUpMapIfNeeded();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_route_gmap, menu);
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
		
		case R.id.play_licence_settings:
			addRouteLines();
			String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(
			        getApplicationContext());
			      AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(this);
			      LicenseDialog.setTitle("Legal Notices");
			      LicenseDialog.setMessage(LicenseInfo);
			      LicenseDialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (gMap == null) {
	        gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	        // Check if we were successful in obtaining the map.
	        if (gMap != null) {
	        	gMap.setMyLocationEnabled(true);
	        	addRouteLines();
	        }
	    }
	}
	
	private void addRouteLines() {
		
		List<MarkerOptions> markers = new ArrayList<MarkerOptions>();
		List<PolylineOptions> walkingPolylines = new ArrayList<PolylineOptions>();
		List<PolylineOptions> busPolylines = new ArrayList<PolylineOptions>();
		LatLng firstLatLng = null;
    	
		int routeIndex = getIntent().getIntExtra(Constants.EXTRA_ROUTE_INDEX, 0);
        List<RouteComponent> routeComponents = GlobalApp.routes.get(routeIndex).routeComponents;
        for (int i = 0, len_i = routeComponents.size(); i < len_i; ++i) {
        	RouteComponent component = routeComponents.get(i);
        	PolylineOptions componentPolys = new PolylineOptions();
        	for (int j = 0, len_j = component.wayPoints.size(); j < len_j; ++j) {
        		WayPoint kkjWayPoint = component.wayPoints.get(j);
        		CoordinatePoint kjjCoordPoint = new CoordinatePoint(Double.parseDouble(kkjWayPoint.y), Double.parseDouble(kkjWayPoint.x));
        		CoordinatePoint wgs84CoordPoint;
        		try {
        			wgs84CoordPoint = CoordinateUtils.convertKKJxyToWGS84lalo(kjjCoordPoint);
        		} catch (CoordinateConversionFailed ccfex) {
        			Log.d("coord conv fail", ccfex.toString());
        			return;
        		}
        		LatLng wayPointLatLng = new LatLng(wgs84CoordPoint.latitude, wgs84CoordPoint.longitude);
        		componentPolys.add(wayPointLatLng);
        		
        		if (firstLatLng == null)
        			firstLatLng = wayPointLatLng;
       			
       			if (!"W".equals(component.code))
       				markers.add(new MarkerOptions()
       					.position(wayPointLatLng)
       					.title(kkjWayPoint.name)
       					.draggable(false)
       					.snippet(kkjWayPoint.time.substring(0, 2) + ":" + kkjWayPoint.time.substring(2, 4)));
        	}
        	
        	if ("W".equals(component.code))
        		walkingPolylines.add(componentPolys);
        	else
        		busPolylines.add(componentPolys);
        }
        
        for (int i = 0, len = markers.size(); i < len; ++i)
        	gMap.addMarker(markers.get(i));
        for (int i = 0, len = walkingPolylines.size(); i < len; ++i)
        	gMap.addPolyline(walkingPolylines.get(i).color(Color.GREEN).width(5));
        for (int i = 0, len = busPolylines.size(); i < len; ++i)
        	gMap.addPolyline(busPolylines.get(i).color(Color.BLUE).width(8));
        
        if (firstLatLng != null)
        	gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 14));

	}

}
