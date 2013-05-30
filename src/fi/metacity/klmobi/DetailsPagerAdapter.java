package fi.metacity.klmobi;

import java.util.Locale;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class DetailsPagerAdapter extends FragmentStatePagerAdapter {

	private final String[] mTitles;
	private int mRouteIndex;

	public DetailsPagerAdapter(FragmentManager fm, String[] titles, int routeIndex) {
		super(fm);
		mTitles = titles;
		mRouteIndex = routeIndex;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0: case 1: case 2:
				return RouteDetailsFragment_.newInstance(mRouteIndex);
		}
		return null;
	}

	@Override
	public int getCount() {
		return 3;
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
