package fi.metacity.klmobi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_routes)
public class RoutesActivity extends SherlockFragmentActivity {
	
	@App
	MHApp mGlobals;
	
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
		showResultsFragment(mExtras);
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
				
			case R.id.later_lines: case R.id.earlier_lines:
				List<Route> routes = mGlobals.getRoutes();
				Route lastRoute = routes.get(routes.size() - 1);
				Date lastStart = lastRoute.routeComponents.get(0).startDateTime;
				
				Date newDateTime;
				if (item.getItemId() == R.id.earlier_lines) {
					Route firstRoute = routes.get(0);
					Date firstStart = firstRoute.routeComponents.get(0).startDateTime;
					
					String currentDateTimeStr = mDate + ";" + mTime;
					Date currentDate = null;
					try {
						currentDate = Utils.dateTimeFormat.parse(currentDateTimeStr);
					} catch (ParseException pex) {
						break;
					}
					
					// Difference between the first and the last of current routes
					long delta = lastStart.getTime() - firstStart.getTime();
					newDateTime = new Date(currentDate.getTime() - delta);
				} else {
					newDateTime = new Date(lastStart.getTime());
				}
				
				Bundle newArgs = getIntent().getExtras();
				
				SimpleDateFormat dateSdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
				String newDate = dateSdf.format(newDateTime);
				
				SimpleDateFormat timeSdf = new SimpleDateFormat("HHmm", Locale.US);
				String newTime = timeSdf.format(newDateTime);
				
				newArgs.putString(Constants.EXTRA_DATE, newDate);
				newArgs.putString(Constants.EXTRA_TIME, newTime);
				
				mGlobals.getRoutes().clear();
				
				// Fetch routes with new date and time
				showResultsFragment(newArgs);
				
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showResultsFragment(Bundle args) {
		Fragment routeResultsFrag = new RouteResultsFragment_();
		routeResultsFrag.setArguments(args);
		getSupportFragmentManager().beginTransaction().
			replace(R.id.route_results_container, routeResultsFrag).commit();
	}

}
