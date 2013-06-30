package fi.metacity.klmobi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.ListAdapter;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EFragment
public class RouteMapDetailsFragment extends ListFragment {
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
		setListShown(false);
		fetchMapImages();
	}

	@Background
	public void fetchMapImages() {
		String lang = "fi".equals(Locale.getDefault().getLanguage()) ? "fi" : "en";
		String detailsXmlRequest = mGlobals.getDetailsXmlRequest();

		Map<String, String> params = new HashMap<String, String>();
		params.put("routeResponse", detailsXmlRequest);
		params.put("startLocation", mGlobals.getStartAddress().streetOnly());
		params.put("endLocation", mGlobals.getEndAddress().streetOnly());
		params.put("routeNumber", String.valueOf(mRouteIndex));
		params.put("language", lang);

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
		if (getActivity() != null) {
			mAdapter = new RouteMapDetailsAdapter(getActivity(), mapComponents);
			setListAdapter(mAdapter);
			setListShown(true);
		}
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
