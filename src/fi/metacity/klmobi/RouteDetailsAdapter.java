package fi.metacity.klmobi;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RouteDetailsAdapter extends BaseExpandableListAdapter {
	private final List<RouteComponent> routeComponents;
	private final Context context;
	
	public RouteDetailsAdapter(Context context, List<RouteComponent> routeComponents) {
		this.routeComponents = routeComponents;
		this.context = context;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return routeComponents.get(groupPosition).wayPoints.get(childPosition);
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
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.route_details_row_child, null);
			
			holder.name = (TextView) v.findViewById(R.id.textView1);
			holder.time = (TextView) v.findViewById(R.id.textView2);
			
			v.setTag(holder);
		}
		else {
			holder = (WayPointHolder) v.getTag();
		}
		
		WayPoint wayPoint = (WayPoint) getChild(groupPosition, childPosition);
		if (childPosition == 0 && wayPoint.name.length() == 0)
			holder.name.setText("// " + context.getResources().getString(R.string.departure));
		else if (childPosition == getChildrenCount(groupPosition)-1 && wayPoint.name.length() == 0)
			holder.name.setText("// " + context.getResources().getString(R.string.arrival));
		else 
			holder.name.setText(wayPoint.name);
		
		holder.time.setText(wayPoint.time.substring(0, 2) + ":" + wayPoint.time.substring(2, 4));

		return v;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return routeComponents.get(groupPosition).wayPoints.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return routeComponents.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return routeComponents.size();
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
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.route_details_row, null);
			
			holder.typeView = (TextView) v.findViewById(R.id.textView1);
			holder.durationView = (TextView) v.findViewById(R.id.textView4);
			holder.distanceView = (TextView) v.findViewById(R.id.textView2);
			holder.startNameView = (TextView) v.findViewById(R.id.textView5);
			holder.endNameView = (TextView) v.findViewById(R.id.textView6);
			holder.startTimeView = (TextView) v.findViewById(R.id.textView7);
			holder.endTimeView = (TextView) v.findViewById(R.id.textView8);
			holder.typeIconView = (ImageView) v.findViewById(R.id.fromFavBtn);
			
			v.setTag(holder);
		}
		else {
			holder = (RouteComponentHolder) v.getTag();
		}
		
		RouteComponent routeComponent = routeComponents.get(groupPosition);
		
		if ("W".equals(routeComponent.code)) {
			holder.typeView.setText(context.getResources().getString(R.string.walking));
			holder.typeIconView.setImageResource(R.drawable.ic_walk);
			holder.typeIconView.setPadding(1, 0, 2, 0);
		} else {
			holder.typeView.setText(context.getResources().getString(R.string.bus) + " " + routeComponent.code);
			holder.typeIconView.setImageResource(R.drawable.ic_bus);
			holder.typeIconView.setPadding(0, 0, 0, 0);
		}
				
		holder.durationView.setText(Long.toString(Math.round(routeComponent.duration)) + " min");
		holder.distanceView.setText(Double.toString(Math.round(routeComponent.distance/100) / 10.0) + " km");

		holder.startNameView.setText(routeComponent.startName);
		holder.endNameView.setText(routeComponent.endName);
		
		holder.startTimeView.setText(KlmobiUtils.time_format.format(routeComponent.startDateTime));
		holder.endTimeView.setText(KlmobiUtils.time_format.format(routeComponent.endDateTime));

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
		TextView typeView, durationView, distanceView;
		TextView startNameView, endNameView;
		TextView startTimeView, endTimeView;
		ImageView typeIconView;
	}
	
	
	private static class WayPointHolder {
		TextView name, time;
	}
}
