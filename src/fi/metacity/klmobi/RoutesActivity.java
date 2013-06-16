package fi.metacity.klmobi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_routes)
public class RoutesActivity extends FragmentActivity {

	@Extra(Constants.EXTRA_ROUTE_INDEX)
	int mInitialRouteIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			showResultsFragment(getIntent().getExtras());
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
	
	private void showResultsFragment(Bundle args) {
		Fragment routeResultsFrag = new RouteResultsFragment_();
		routeResultsFrag.setArguments(args);
		getSupportFragmentManager().beginTransaction().
			replace(R.id.route_details_container, routeResultsFrag).commit();
	}

}
