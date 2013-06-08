package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import fi.sandman.utils.coordinate.CoordinateConversionFailed;
import fi.sandman.utils.coordinate.CoordinatePoint;
import fi.sandman.utils.coordinate.CoordinateUtils;

public class RouteGMapFragment extends SupportMapFragment {

	private MHApp mGlobals;
	private int mRouteIndex;
	private GoogleMap mGmap;

	public static RouteGMapFragment newInstance(int routeIndex) {
		RouteGMapFragment gmapFragment = new RouteGMapFragment();
		Bundle args = new Bundle();
		args.putInt(Constants.EXTRA_ROUTE_INDEX, routeIndex);
		gmapFragment.setArguments(args);
		return gmapFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRouteIndex = getArguments().getInt(Constants.EXTRA_ROUTE_INDEX);
	}

	@Override
	public void onStart() {
		super.onStart();
		mGlobals = (MHApp) getActivity().getApplication();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mGmap == null) {
			mGmap = getMap();
			// Check if we were successful in obtaining the map.
			if (mGmap != null) {
				mGmap.setMyLocationEnabled(true);
				addRouteLines();
			}
		}
	}

	private void addRouteLines() {
		BitmapDescriptor busStopMarker = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker);

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
				CoordinatePoint kjjCoordPoint = new CoordinatePoint(Double.parseDouble(kkjWayPoint.y), 
						Double.parseDouble(kkjWayPoint.x));
				CoordinatePoint wgs84CoordPoint;
				try {
					wgs84CoordPoint = CoordinateUtils.convertKKJxyToWGS84lalo(kjjCoordPoint);
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
					markers.add(new MarkerOptions()
					.position(wayPointLatLng)
					.title(kkjWayPoint.name)
					.icon(busStopMarker)
					.draggable(false)
					.snippet(kkjWayPoint.time.substring(0, 2) + ":" + kkjWayPoint.time.substring(2, 4)));
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
		for (int i = 0, len = walkingPolylines.size(); i < len; ++i) {
			mGmap.addPolyline(walkingPolylines.get(i).color(Color.GREEN).width(5));
		}
		for (int i = 0, len = busPolylines.size(); i < len; ++i) {
			mGmap.addPolyline(busPolylines.get(i).color(Color.BLUE).width(8));
		}

		if (firstLatLng != null) {
			mGmap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 14));
		}
	}

}
