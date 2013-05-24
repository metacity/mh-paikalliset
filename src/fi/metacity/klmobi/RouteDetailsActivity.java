package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import android.R.color;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TableLayout.LayoutParams;

public class RouteDetailsActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	private int mSelectedCityIndex;
	private int mRouteIndex;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_details);
		
		if (GlobalApp.startAddress == null 
		 || GlobalApp.endAddress == null
		 || GlobalApp.routes == null
		 || GlobalApp.detailsXmlRequest == null) {
		
			finish();
			return;
		}

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		AQuery aq = new AQuery(this);
		aq.hardwareAccelerated11();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		mSelectedCityIndex = getIntent().getIntExtra(Constants.EXTRA_CITY_INDEX, 0);
		mRouteIndex = getIntent().getIntExtra(Constants.EXTRA_ROUTE_INDEX, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_route_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		
		case R.id.showmap_settings:
			Intent intent = new Intent(this, RouteGMapActivity.class);
			intent.putExtra(Constants.EXTRA_ROUTE_INDEX, mRouteIndex);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Bundle args = new Bundle();
			args.putInt(Constants.EXTRA_CITY_INDEX, mSelectedCityIndex);
			args.putInt(Constants.EXTRA_ROUTE_INDEX, mRouteIndex);
			
			Fragment fragment = (position == 0) ? new RouteDetailsFragment() : new RouteMapDetailsFragment();
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.routeDetailsTitle).toUpperCase(Locale.ENGLISH);
				case 1:
					return getString(R.string.routeMapDetailsTitle).toUpperCase(Locale.ENGLISH);
			}
			return null;
		}
	}

	public static class RouteDetailsFragment extends Fragment {

		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			if (GlobalApp.routes == null)
				return null;
			
			int routeIndex = getArguments().getInt(Constants.EXTRA_ROUTE_INDEX);
			
			ExpandableListView expandableComponentView = new ExpandableListView(getActivity()); 
			expandableComponentView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			expandableComponentView.setGroupIndicator(null);
			expandableComponentView.setBackgroundColor(color.background_light);
			expandableComponentView.setCacheColorHint(color.background_light);
			
			List<RouteComponent> routeComponents = GlobalApp.routes.get(routeIndex).routeComponents;
			expandableComponentView.setAdapter(new RouteDetailsAdapter(getActivity(), routeComponents));
			
			for (int i = expandableComponentView.getExpandableListAdapter().getGroupCount()-1; i > -1; --i)
				expandableComponentView.expandGroup(i, true);

			return expandableComponentView;
		}
	}
	
	public static class RouteMapDetailsFragment extends ListFragment {

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			if (GlobalApp.startAddress == null 
			 || GlobalApp.endAddress == null
			 || GlobalApp.detailsXmlRequest == null) {
				return;
			}
			
			ListView lv = getListView();
			lv.setBackgroundColor(color.background_light);
			lv.setCacheColorHint(color.background_light);
			
			final AQuery aq = new AQuery(getActivity());
			final String lang = "fi".equals(Locale.getDefault().getLanguage()) ? "fi" : "en";
			final String url = "http://" + Constants.citySubdomains[getArguments().getInt(Constants.EXTRA_CITY_INDEX)] 
					+".matkahuolto.info/" + lang + "/print/";
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("startLocation", GlobalApp.startAddress.streetOnly());
			params.put("endLocation", GlobalApp.endAddress.streetOnly());
			params.put("routeNumber", getArguments().getInt(Constants.EXTRA_ROUTE_INDEX));
			params.put("routeResponse", GlobalApp.detailsXmlRequest);
			params.put("language", lang);
			
			aq.ajax(url, params, String.class, new AjaxCallback<String>() {

				@Override
				public void callback(String url, String html, AjaxStatus status) {
					if (html == null) {
						Toast.makeText(getActivity(), Integer.toString(status.getCode()), Toast.LENGTH_LONG).show();
						return;
					}
					List<MapComponent> mapComponents = new ArrayList<MapComponent>();
					Document doc = Jsoup.parse(html);
					
					Elements imageRows = doc.select("#RouteImage").select(".LegRowOdd, .LegRowEven");
					String imageUrl = "", time = "", type = "", location = "";
					for (int i = 0, len = imageRows.size(); i < len; ++i) {
						Element imageRow = imageRows.get(i);
						if (i % 2 == 0) {
							time = imageRow.select("td[class=LineStartTime]").text();
							type = imageRow.select("td[class=LineTransportTypeInformation]").text();
							location = imageRow.select("td[class=LineStartLocation]").text();
						} else {
							imageUrl = imageRow.select("img:eq(0)").attr("src");
							mapComponents.add(new MapComponent(imageUrl, time, type, location));
						}
					}

					setListAdapter(new RouteMapDetailsAdapter(getActivity(), mapComponents));
				}
			});
			
		}
	}

}
