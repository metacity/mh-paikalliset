package fi.metacity.klmobi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

@EActivity(R.layout.activity_routes)
public class RoutesActivity extends SherlockFragmentActivity {
	
	@App
	MHApp mGlobals;
	
	@ViewById(R.id.pager)
	ViewPager mPager;
	
	@ViewById(R.id.tabs)
	PagerSlidingTabStrip mTabs;
	
	@Extra(Constants.EXTRA_DATE)
	String mDate;
	
	@Extra(Constants.EXTRA_TIME)
	String mTime;
	
	private Bundle mExtras; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mExtras = getIntent().getExtras();
		if (savedInstanceState == null) {
			showResultsFragment(mExtras);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getSupportMenuInflater().inflate(R.menu.routes, menu);
		return true;
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
	public void showViewPagerIfNeeded() {
		if (mPager != null && mTabs != null) {
			/*DetailsPagerAdapter adapter = new DetailsPagerAdapter(
					getSupportFragmentManager(), new String[] { "Reittitiedot", "Vaihtokuvat", "Kartta" }, -1);
			mPager.setAdapter(adapter);
			mTabs.setViewPager(mPager);*/
		}
	}
	
	private void showResultsFragment(Bundle args) {
		Fragment routeResultsFrag = new RouteResultsFragment_();
		routeResultsFrag.setArguments(args);
		getSupportFragmentManager().beginTransaction().
			replace(R.id.route_results_container, routeResultsFrag).commit();
	}

}
