package fi.metacity.klmobi;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RouteDetailsAdapter extends BaseExpandableListAdapter {
	
	private final List<RouteComponent> mRouteComponents;
	private final Context mContext;
	
	private final int m3Dps;
	
	public RouteDetailsAdapter(Context context, List<RouteComponent> routeComponents) {
		mRouteComponents = routeComponents;
		mContext = context;
		
		m3Dps = Utils.dpsToPixels(context, 3);
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mRouteComponents.get(groupPosition).wayPoints.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
			View convertView, ViewGroup parent) {
		View v = convertView;
		WayPointHolder holder;
		
		if (v == null) {
			holder = new WayPointHolder();
			v = View.inflate(mContext, R.layout.route_details_row_child, null);
			
			holder.name = (TextView) v.findViewById(R.id.detailsRowChildStreet);
			holder.time = (TextView) v.findViewById(R.id.detailsRowChildTime);
			holder.connectorIcon = (ImageView) v.findViewById(R.id.detailsRowChildConnectorIcon);
			
			v.setTag(holder);
		}
		else {
			holder = (WayPointHolder) v.getTag();
		}

		if ("W".equals(mRouteComponents.get(groupPosition).code)) {
			holder.connectorIcon.setImageResource(R.drawable.ic_waypoint_connector_walk);
		} else {
			holder.connectorIcon.setImageResource(R.drawable.ic_waypoint_connector_bus);
		}
		
		WayPoint wayPoint = (WayPoint) getChild(groupPosition, childPosition);
		if (childPosition == 0 && wayPoint.name.length() == 0)
			holder.name.setText("// " + mContext.getResources().getString(R.string.departure));
		else if (childPosition == getChildrenCount(groupPosition)-1 && wayPoint.name.length() == 0)
			holder.name.setText("// " + mContext.getResources().getString(R.string.arrival));
		else 
			holder.name.setText(wayPoint.name);
		
		holder.time.setText(wayPoint.time.substring(0, 2) + ":" + wayPoint.time.substring(2, 4));

		return v;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mRouteComponents.get(groupPosition).wayPoints.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mRouteComponents.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mRouteComponents.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View v = convertView;
		RouteComponentHolder holder;
		
		if (v == null) {
			holder = new RouteComponentHolder();
			v = View.inflate(mContext, R.layout.route_details_row, null);
			
			holder.typeView = (TextView) v.findViewById(R.id.detailsRowType);
			holder.durationView = (TextView) v.findViewById(R.id.detailsRowDuration);
			holder.distanceView = (TextView) v.findViewById(R.id.detailsRowDist);
			holder.startNameView = (TextView) v.findViewById(R.id.detailsRowStartStreet);
			holder.endNameView = (TextView) v.findViewById(R.id.detailsRowEndStreet);
			holder.startTimeView = (TextView) v.findViewById(R.id.detailsRowStartTime);
			holder.endTimeView = (TextView) v.findViewById(R.id.detailsRowEndTime);
			holder.typeIconView = (ImageView) v.findViewById(R.id.detailsRowTypeIcon);
			
			v.setTag(holder);
		}
		else {
			holder = (RouteComponentHolder) v.getTag();
		}
		
		RouteComponent routeComponent = mRouteComponents.get(groupPosition);
		
		if ("W".equals(routeComponent.code)) {
			holder.typeView.setText(mContext.getResources().getString(R.string.walking));
			holder.typeIconView.setImageResource(R.drawable.ic_walk);
			holder.typeIconView.setPadding(m3Dps, 0, m3Dps, 0);
		} else {
			holder.typeView.setText(mContext.getResources().getString(R.string.bus) + " " + routeComponent.code);
			holder.typeIconView.setImageResource(R.drawable.ic_bus);
			holder.typeIconView.setPadding(0, 0, 0, 0);
		}
				
		holder.durationView.setText(Long.toString(Math.round(routeComponent.duration)) + " min");
		holder.distanceView.setText(Double.toString(Math.round(routeComponent.distance/100) / 10.0) + " km");

		holder.startNameView.setText(routeComponent.startName);
		holder.endNameView.setText(routeComponent.endName);
		
		holder.startTimeView.setText(Utils.timeFormat.format(routeComponent.startDateTime));
		holder.endTimeView.setText(Utils.timeFormat.format(routeComponent.endDateTime));

		return v;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	
	private static class RouteComponentHolder {
		TextView typeView;
		TextView durationView;
		TextView distanceView;
		TextView startNameView;
		TextView endNameView;
		TextView startTimeView;
		TextView endTimeView;
		ImageView typeIconView;
	}
	
	
	private static class WayPointHolder {
		TextView name;
		TextView time;
		ImageView connectorIcon;
	}
}
