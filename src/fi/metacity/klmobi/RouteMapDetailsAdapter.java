package fi.metacity.klmobi;

import java.util.List;

import com.androidquery.AQuery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RouteMapDetailsAdapter extends ArrayAdapter<MapComponent> {
	private final AQuery aq;
	private final List<MapComponent> mapComponents;
	private final Context context;
	
	public RouteMapDetailsAdapter(Context context, List<MapComponent> mapComponents) {
		super(context, R.layout.route_mapdetails_row, mapComponents);
		this.mapComponents = mapComponents;
		this.context = context;
		this.aq = new AQuery(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		MapComponentHolder holder;
		
		if (v == null) {
			holder = new MapComponentHolder();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.route_mapdetails_row, null);
			
			holder.timeView = (TextView) v.findViewById(R.id.textView1);
			holder.typeView = (TextView) v.findViewById(R.id.textView2);
			holder.locationView = (TextView) v.findViewById(R.id.textView3);
			holder.imageView = (ImageView) v.findViewById(R.id.fromFavBtn);
			holder.progressBarView = (ProgressBar) v.findViewById(R.id.progressBar1);
			
			v.setTag(holder);
		}
		else {
			holder = (MapComponentHolder) v.getTag();
		}
		
		MapComponent mapComponent = mapComponents.get(position);

		holder.timeView.setText(mapComponent.time);
		holder.typeView.setText(mapComponent.type);
		holder.locationView.setText(mapComponent.location);
		aq.id(holder.imageView).progress(holder.progressBarView).image(mapComponent.imageUrl, true, true);
		
		return v;
	}
	
	private static class MapComponentHolder {
		TextView timeView, typeView, locationView;
		ImageView imageView;
		ProgressBar progressBarView;
	}
}
