package fi.metacity.klmobi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;

import com.actionbarsherlock.app.SherlockListFragment;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EFragment
public class RouteMapDetailsFragment extends SherlockListFragment {
	private static final String TAG = "RouteMapDetailsFragment";

	@App
	MHApp mGlobals;
	
	@Pref
	Preferences_ mPreferences;
	
	@FragmentArg(Constants.EXTRA_ROUTE_INDEX)
	int mRouteIndex;
	
	private ListAdapter mAdapter;

	public static RouteMapDetailsFragment_ newInstance(int position) {
		RouteMapDetailsFragment_ mapsFragment = new RouteMapDetailsFragment_();
		Bundle args = new Bundle();
		args.putInt(Constants.EXTRA_ROUTE_INDEX, position);
		mapsFragment.setArguments(args);
		return mapsFragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//ListView lv = getListView();
		setListShown(false);
		fetchMapImages();
	}
	
	@Background
	public void fetchMapImages() {
		String lang = "fi".equals(Locale.getDefault().getLanguage()) ? "fi" : "en";
		String detailsXmlRequest = mGlobals.getDetailsXmlRequest();
		detailsXmlRequest = detailsXmlRequest.replace("mtrxml", "MTRXML");
		detailsXmlRequest = detailsXmlRequest.replace("route", "ROUTE");
		detailsXmlRequest = detailsXmlRequest.replace("length", "LENGTH");
		detailsXmlRequest = detailsXmlRequest.replace("service", "SERVICE");
		detailsXmlRequest = detailsXmlRequest.replace("<isa", "<ISA");
		detailsXmlRequest = detailsXmlRequest.replace("maploc", "MAPLOC");
		detailsXmlRequest = detailsXmlRequest.replace("name", "LANG");
		detailsXmlRequest = detailsXmlRequest.replace("point", "POINT");
		detailsXmlRequest = detailsXmlRequest.replace("arrival", "ARRIVAL");
		detailsXmlRequest = detailsXmlRequest.replace("departure", "DEPARTURE");
		detailsXmlRequest = detailsXmlRequest.replace("sref", "SREF");
		detailsXmlRequest = detailsXmlRequest.replace("walk", "WALK");
		detailsXmlRequest = detailsXmlRequest.replace("stop", "STOP");
		detailsXmlRequest = detailsXmlRequest.replace("walk", "WALK");
		detailsXmlRequest = detailsXmlRequest.replace("line", "LINE");
		detailsXmlRequest = detailsXmlRequest.replace("xtra", "XTRA");
		
		Map<String, String> params = new LinkedHashMap<String, String>();

		params.put("routeResponse", detailsXmlRequest);
		params.put("startLocation", mGlobals.getStartAddress().streetOnly());
		params.put("endLocation", mGlobals.getEndAddress().streetOnly());
		params.put("routeNumber", String.valueOf(mRouteIndex));
		params.put("language", lang);
		
		// TODO: EI TOIMI
		
		Log.i(TAG,  mRouteIndex + " -- " +  detailsXmlRequest.substring(detailsXmlRequest.length()-5, detailsXmlRequest.length()));
		Log.i(TAG, params.size() + "");
		try {
			String responseHtml = Utils.httpPost(mPreferences.baseUrl().get() + lang + "/print/", params);
			List<MapComponent> mapComponents = buildMapList(responseHtml);
			setMapAdapter(mapComponents);
		} catch (IOException ioex) {
			Log.e(TAG, ioex.toString());
			ioex.printStackTrace();
		}
	}

	@UiThread
	public void setMapAdapter(List<MapComponent> mapComponents) {
		mAdapter = new RouteMapDetailsAdapter(getSherlockActivity(), mapComponents);
		setListAdapter(mAdapter);
		setListShown(true);
	}

	private List<MapComponent> buildMapList(String responseHtml) {
		List<MapComponent> mapComponents = new ArrayList<MapComponent>();
		Document doc = Jsoup.parse(responseHtml);
		
		String imageUrl = "";
		String time = "";
		String type = "";
		String location = "";
		Elements imageRows = doc.select("#RouteImage").select(".LegRowOdd, .LegRowEven");
		for (int i = 0, len = imageRows.size(); i < len; ++i) {
			Element imageRow = imageRows.get(i);
			if ((i & 1) == 0) {
				time = imageRow.select("td[class=LineStartTime]").text();
				type = imageRow.select("td[class=LineTransportTypeInformation]").text();
				location = imageRow.select("td[class=LineStartLocation]").text();
			} else {
				imageUrl = imageRow.select("img:eq(0)").attr("src");
				mapComponents.add(new MapComponent(imageUrl, time, type, location));
			}
		}

		return mapComponents;
	}

}
