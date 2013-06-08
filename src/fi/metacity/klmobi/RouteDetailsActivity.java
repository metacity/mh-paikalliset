package fi.metacity.klmobi;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_route_details)
public class RouteDetailsActivity extends SherlockFragmentActivity {

	@App
	MHApp mGlobals;

	@Extra(Constants.EXTRA_ROUTE_INDEX)
	int mRouteIndex;

	@ViewById(R.id.pager)
	ViewPager mPager;

	@ViewById(R.id.tabs)
	PagerSlidingTabStrip mTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@AfterViews
	public void setupPagerAndTabs() {
		RouteDetailsPagerAdapter adapter = new RouteDetailsPagerAdapter(
				getSupportFragmentManager(), 
				new String[] { 
					getString(R.string.routeDetailsTitle), 
					getString(R.string.transferImages), 
					getString(R.string.title_activity_route_gmap) 
				}, 
				mRouteIndex
				); 
		mPager.setAdapter(adapter);
		mTabs.setViewPager(mPager);
		mPager.setCurrentItem(0, true);
	}
}
