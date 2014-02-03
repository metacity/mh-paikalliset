package fi.metacity.klmobi;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.BooleanRes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

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
