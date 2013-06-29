package fi.metacity.klmobi;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteAdapter extends ArrayAdapter<Route> {
	private final List<Route> mRoutes;
	private final Context mContext;
	
	public RouteAdapter(Context context, List<Route> routes) {
		super(context, R.layout.route_results_row, routes);
		mRoutes = routes;
		mContext = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		RouteHolder holder;
		
		if (v == null) {
			holder = new RouteHolder();
			v = View.inflate(mContext, R.layout.route_results_row, null);
			
			holder.departureView = (TextView) v.findViewById(R.id.departure);
			holder.arrivalView = (TextView) v.findViewById(R.id.arrival);
			holder.durationView = (TextView) v.findViewById(R.id.duration);
			holder.stopDepView = (TextView) v.findViewById(R.id.stopDep);
			holder.stopArrView = (TextView) v.findViewById(R.id.stopArr);
			holder.walkingDistView = (TextView) v.findViewById(R.id.walkingDist);
			holder.routeStepsLayout = (LinearLayout) v.findViewById(R.id.routeStepsLayout);
			
			v.setTag(holder);
		}
		else {
			holder = (RouteHolder) v.getTag();
		}
		
		Route route = mRoutes.get(position);
		
		String departureTime = Utils.timeFormat.format(route.routeComponents.get(0).startDateTime);
		holder.departureView.setText(departureTime);
		
		String arrivalTime = Utils.timeFormat.format(
				route.routeComponents.get(route.routeComponents.size()-1).endDateTime);
		holder.arrivalView.setText(arrivalTime);

		long duration = Math.round(route.duration);
		holder.durationView.setText((duration > 59 ? Long.toString(duration / 60) + " h " : "") 
				+ String.valueOf(duration % 60) + " min");
		
		holder.routeStepsLayout.removeAllViews();
		float walkingDistance = 0.0f;
		int firstBusIndex = -1, lastBusIndex = -1;
		for (int i = 0, len = route.routeComponents.size(); i < len; ++i) {
			RouteComponent routeComp = route.routeComponents.get(i);
			if ("W".equals(routeComp.code)) { // Walking
				ImageView walkIcon = new ImageView(this.mContext);
				walkIcon.setImageResource(R.drawable.ic_walk);
				walkIcon.setPadding(5, 5, 5, 5);
				holder.routeStepsLayout.addView(walkIcon);
				walkingDistance += routeComp.distance;
			} else { // Bus
				if (firstBusIndex < 0) {
					firstBusIndex = i;
				}
				lastBusIndex = i;
				
				LinearLayout busIconLayout = new LinearLayout(this.mContext);
				busIconLayout.setOrientation(LinearLayout.VERTICAL);
				busIconLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				busIconLayout.setPadding(5, 5, 5, 5);
				
				ImageView busIcon = new ImageView(mContext);
				busIcon.setImageResource(R.drawable.ic_bus);
				busIconLayout.addView(busIcon);
				
				TextView lineNumber = new TextView(mContext);
				lineNumber.setTextSize(13);
				lineNumber.setTypeface(null, Typeface.BOLD);
				lineNumber.setText(routeComp.code);
				lineNumber.setGravity(Gravity.CENTER_HORIZONTAL);
				busIconLayout.addView(lineNumber);

				
				holder.routeStepsLayout.addView(busIconLayout);
			}
		}
		String firstStopDepartureTime = (firstBusIndex == -1) ? "-" : Utils.timeFormat.format(
				route.routeComponents.get(firstBusIndex).startDateTime);
		holder.stopDepView.setText("(" + firstStopDepartureTime + ")");
		
		String lastStopArrivalTime = (lastBusIndex == -1) ? "-" : Utils.timeFormat.format(
				route.routeComponents.get(lastBusIndex).endDateTime);
		holder.stopArrView.setText("(" + lastStopArrivalTime + ")");
		
		holder.walkingDistView.setText(Double.toString(Math.round(walkingDistance/100) / 10.0) + " km");
		
		v.setBackgroundResource(route.isSelected ? R.color.lightOrange : R.drawable.selector_orange);

		return v;
	}
	
	public static class RouteHolder {
		TextView departureView, arrivalView;
		TextView durationView;
		TextView stopDepView, stopArrView;
		TextView walkingDistView;
		LinearLayout routeStepsLayout;
	}
}
