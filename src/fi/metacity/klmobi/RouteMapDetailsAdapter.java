package fi.metacity.klmobi;

import java.util.List;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

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
		MapComponentHolder holder;
		
		if (v == null) {
			holder = new MapComponentHolder();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.route_mapdetails_row, null);
			
			holder.timeView = (TextView) v.findViewById(R.id.mapDetailTime);
			holder.typeView = (TextView) v.findViewById(R.id.mapDetailType);
			holder.locationView = (TextView) v.findViewById(R.id.mapDetailPlace);
			holder.mapImage = (ImageView) v.findViewById(R.id.mapDetailImage);
			
			v.setTag(holder);
		}
		else {
			holder = (MapComponentHolder) v.getTag();
		}
		
		MapComponent mapComponent = mMapComponents.get(position);

		holder.timeView.setText(mapComponent.time);
		holder.typeView.setText(mapComponent.type);
		holder.locationView.setText(mapComponent.location);
		UrlImageViewHelper.setUrlDrawable(holder.mapImage, mapComponent.imageUrl, null, 60000);
		
		return v;
	}
	
	private static class MapComponentHolder {
		TextView timeView;
		TextView typeView;
		TextView locationView;
		ImageView mapImage;
	}
}
