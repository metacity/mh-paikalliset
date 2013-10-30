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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RouteAdapter extends ArrayAdapter<Route> {
	private final List<Route> mRoutes;
	private final Context mContext;
	
	private final Typeface mRobotoCondensedBold;
	private final int m3Dps;
	
	public RouteAdapter(Context context, List<Route> routes) {
		super(context, R.layout.route_results_row, routes);
		mRoutes = routes;
		mContext = context;
		
		mRobotoCondensedBold = Typeface.create("sans-serif-condensed", Typeface.BOLD);
		m3Dps = Utils.dpsToPixels(context, 3);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		RouteHolder holder;
		
		if (v == null) {
			holder = new RouteHolder();
			v = View.inflate(mContext, R.layout.route_results_row, null);
			
			holder.rowLayout = (RelativeLayout) v.findViewById(R.id.routeRowLayout);
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
		holder.durationView.setText((duration > 59 ? String.valueOf(duration / 60) + " h " : "") 
				+ String.valueOf(duration % 60) + " min");
		
		holder.routeStepsLayout.removeAllViews();
		float walkingDistance = 0.0f;
		int firstBusIndex = -1, lastBusIndex = -1;
		for (int i = 0, len = route.routeComponents.size(); i < len; ++i) {
			RouteComponent routeComp = route.routeComponents.get(i);
			if ("W".equals(routeComp.code)) { // Walking
				ImageView walkIcon = new ImageView(this.mContext);
				walkIcon.setImageResource(R.drawable.ic_walk);
				walkIcon.setPadding(m3Dps, m3Dps, m3Dps, m3Dps);
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
				busIconLayout.setPadding(m3Dps, m3Dps, m3Dps, m3Dps);
				
				ImageView busIcon = new ImageView(mContext);
				busIcon.setImageResource(R.drawable.ic_bus);
				busIconLayout.addView(busIcon);
				
				TextView lineNumber = new TextView(mContext);
				lineNumber.setTextSize(15);
				lineNumber.setTypeface(mRobotoCondensedBold, Typeface.BOLD);
				lineNumber.setText(routeComp.code);
				lineNumber.setGravity(Gravity.CENTER_HORIZONTAL);
				busIconLayout.addView(lineNumber);

				
				holder.routeStepsLayout.addView(busIconLayout);
			}
		}
		
		if (firstBusIndex != -1 && lastBusIndex != -1) {
			String firstStopDepartureTime = Utils.timeFormat.format(
					route.routeComponents.get(firstBusIndex).startDateTime);
			holder.stopDepView.setText(" " + firstStopDepartureTime);
			holder.stopDepView.setVisibility(View.VISIBLE);
			
			String lastStopArrivalTime = Utils.timeFormat.format(
					route.routeComponents.get(lastBusIndex).endDateTime);
			holder.stopArrView.setText(lastStopArrivalTime + " ");
			holder.stopArrView.setVisibility(View.VISIBLE);
		} else {
			holder.stopDepView.setVisibility(View.INVISIBLE);
			holder.stopArrView.setVisibility(View.INVISIBLE);
		}

		holder.walkingDistView.setText(Double.toString(Math.round(walkingDistance/100) / 10.0) + " km ");
		holder.rowLayout.setBackgroundResource(route.isSelected ? R.drawable.button_orange_bordered 
				: R.drawable.button_white_orange_bordered);

		return v;
	}
	
	public static class RouteHolder {
		RelativeLayout rowLayout;
		TextView departureView, arrivalView;
		TextView durationView;
		TextView stopDepView, stopArrView;
		TextView walkingDistView;
		LinearLayout routeStepsLayout;
	}
}
