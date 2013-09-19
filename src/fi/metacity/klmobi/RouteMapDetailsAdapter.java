package fi.metacity.klmobi;

import java.util.List;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RouteMapDetailsAdapter extends ArrayAdapter<MapComponent> {
	private final List<MapComponent> mMapComponents;
	private final Context mContext;
	
	public RouteMapDetailsAdapter(Context context, List<MapComponent> mapComponents) {
		super(context, R.layout.route_mapdetails_row, mapComponents);
		mMapComponents = mapComponents;
		mContext = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			MapComponentHolder holder = new MapComponentHolder();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.route_mapdetails_row, null);
			
			holder.timeView = (TextView) v.findViewById(R.id.mapDetailTime);
			holder.typeView = (TextView) v.findViewById(R.id.mapDetailType);
			holder.locationView = (TextView) v.findViewById(R.id.mapDetailPlace);
			holder.mapImage = (ImageView) v.findViewById(R.id.mapDetailImage);
			holder.progressBar = (ProgressBar) v.findViewById(R.id.mapDetailProgressBar);
			
			v.setTag(holder);
		} 
		final MapComponentHolder mapHolder = (MapComponentHolder) v.getTag();
		
		MapComponent mapComponent = mMapComponents.get(position);

		mapHolder.timeView.setText(mapComponent.time);
		mapHolder.typeView.setText(mapComponent.type);
		mapHolder.locationView.setText(mapComponent.location);
		Picasso.with(mContext).load(mapComponent.imageUrl).into(mapHolder.mapImage, new Callback() {

			@Override
			public void onSuccess() {
				mapHolder.progressBar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onError() {
			}
		});
		
		return v;
	}
	
	private static class MapComponentHolder {
		TextView timeView;
		TextView typeView;
		TextView locationView;
		ImageView mapImage;
		ProgressBar progressBar;
	}
}
