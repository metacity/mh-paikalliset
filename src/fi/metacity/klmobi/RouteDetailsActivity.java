package fi.metacity.klmobi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.LongClick;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.BooleanRes;

@EActivity(R.layout.activity_route_details)
public class RouteDetailsActivity extends FragmentActivity {

	@App
	MHApp mGlobals;
	
	@BooleanRes(R.bool.has_two_panes)
	boolean mIsDualPane;

	@Extra(Constants.EXTRA_ROUTE_INDEX)
	int mRouteIndex;

	@ViewById(R.id.pager)
	ViewPager mPager;

	@ViewById(R.id.tabs)
	PagerSlidingTabStrip mTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (mIsDualPane) {
			NavUtils.navigateUpFromSameTask(this);
			return;
		}
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
					getString(R.string.transferImages)
				}, 
				mRouteIndex
				); 
		mPager.setAdapter(adapter);
		mTabs.setViewPager(mPager);
		mPager.setCurrentItem(0, true);
	}
	
	@Click(R.id.showInMapBtn)
	public void showInGoogleMap() {
		RouteGMapActivity_.intent(this).mRouteIndex(mRouteIndex).flags(Intent.FLAG_ACTIVITY_NO_HISTORY).start();
	}
	
	@LongClick(R.id.showInMapBtn)
	public void showInGoogleMapTooltip() {
		Toast.makeText(this, getString(R.string.showInMap), Toast.LENGTH_SHORT).show();
	}
}
