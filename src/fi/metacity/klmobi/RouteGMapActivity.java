package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.res.BooleanRes;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

import fi.sandman.utils.coordinate.*;

@EActivity
@OptionsMenu(R.menu.activity_route_gmap)
public class RouteGMapActivity extends FragmentActivity {
	
	@App
	MHApp mGlobals;
	
	@BooleanRes(R.bool.has_two_panes)
	boolean mIsDualPane;
	
	@Extra(Constants.EXTRA_ROUTE_INDEX)
	int mRouteIndex;
	
	private GoogleMap mGmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_gmap);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		setUpMapIfNeeded();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (mIsDualPane) {
					NavUtils.navigateUpTo(this, RoutesActivity_.intent(this).mInitialRouteIndex(mRouteIndex).get());
				} else {
					Intent intent = new Intent(this, RouteDetailsActivity_.class);
					intent.putExtra(Constants.EXTRA_ROUTE_INDEX, mRouteIndex);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
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
	
	public void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mGmap == null) {
	        mGmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	        // Check if we were successful in obtaining the map.
	        if (mGmap != null) {
	        	mGmap.setMyLocationEnabled(true);
	        	addRouteLines();
	        }
	    }
	}
	
	private void addRouteLines() {
		BitmapDescriptor busStopMarker = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker);
		BitmapDescriptor busStopMarkerStart = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker_start);
		BitmapDescriptor busStopMarkerEnd = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker_end);

		List<MarkerOptions> markers = new ArrayList<MarkerOptions>();
		List<PolylineOptions> walkingPolylines = new ArrayList<PolylineOptions>();
		List<PolylineOptions> busPolylines = new ArrayList<PolylineOptions>();
		LatLng firstLatLng = null;

		List<RouteComponent> routeComponents = mGlobals.getRoutes().get(mRouteIndex).routeComponents;
		for (int i = 0, len_i = routeComponents.size(); i < len_i; ++i) {
			RouteComponent component = routeComponents.get(i);
			PolylineOptions componentPolys = new PolylineOptions();
			for (int j = 0, len_j = component.wayPoints.size(); j < len_j; ++j) {
				WayPoint kkjWayPoint = component.wayPoints.get(j);
				CoordinatePoint kkjCoordPoint = new CoordinatePoint(Double.parseDouble(kkjWayPoint.y), 
						Double.parseDouble(kkjWayPoint.x));
				CoordinatePoint wgs84CoordPoint;
				try {
					wgs84CoordPoint = CoordinateUtils.convertKKJxyToWGS84lalo(kkjCoordPoint);
				} catch (CoordinateConversionFailed ccfex) {
					Log.d("coord conv fail", ccfex.toString());
					return;
				}
				LatLng wayPointLatLng = new LatLng(wgs84CoordPoint.latitude, wgs84CoordPoint.longitude);
				componentPolys.add(wayPointLatLng);

				if (firstLatLng == null) {
					firstLatLng = wayPointLatLng;
				}

				if (!"W".equals(component.code)) {
					MarkerOptions markerOptions = new MarkerOptions()
					.position(wayPointLatLng)
					.title(kkjWayPoint.name)
					.draggable(false)
					.snippet(kkjWayPoint.time.substring(0, 2) + ":" + kkjWayPoint.time.substring(2, 4));
					if (j == 0) {
						markerOptions.icon(busStopMarkerStart);
					} else if (j == len_j - 1) {
						markerOptions.icon(busStopMarkerEnd);
					} else {
						markerOptions.icon(busStopMarker);
					}
					markers.add(markerOptions);
				}
			}

			if ("W".equals(component.code)) {
				walkingPolylines.add(componentPolys);
			} else {
				busPolylines.add(componentPolys);
			}
		}

		for (int i = 0, len = markers.size(); i < len; ++i) {
			mGmap.addMarker(markers.get(i));
		}
		
		int walkingLineWidth = Utils.dpsToPixels(this, 3);
		for (int i = 0, len = walkingPolylines.size(); i < len; ++i) {
			mGmap.addPolyline(walkingPolylines.get(i).color(Color.MAGENTA).width(walkingLineWidth));
		}
		
		int busLineWidth = Utils.dpsToPixels(this, 4);
		for (int i = 0, len = busPolylines.size(); i < len; ++i) {
			mGmap.addPolyline(busPolylines.get(i).color(Color.BLUE).width(busLineWidth));
		}

		if (firstLatLng != null) {
			mGmap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 15));
		}
	}

}
