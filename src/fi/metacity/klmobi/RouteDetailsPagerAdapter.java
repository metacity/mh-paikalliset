package fi.metacity.klmobi;

import java.util.Locale;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class RouteDetailsPagerAdapter extends FragmentStatePagerAdapter {

	private final String[] mTitles;
	private int mRouteIndex;

	public RouteDetailsPagerAdapter(FragmentManager fm, String[] titles, int routeIndex) {
		super(fm);
		mTitles = titles;
		mRouteIndex = routeIndex;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return RouteDetailsFragment_.newInstance(mRouteIndex);
				
			case 1:
				return RouteMapDetailsFragment_.newInstance(mRouteIndex);
				
//			case 2:
//				return RouteGMapFragment.newInstance(mRouteIndex);
		}
		return null;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTitles[position].toUpperCase(Locale.ENGLISH);
	}

	public void updateRouteIndex(int index) {
		mRouteIndex = index;
		notifyDataSetChanged();
	}

}
