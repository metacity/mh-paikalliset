package fi.metacity.klmobi;

import java.util.Calendar;

import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import android.text.Editable;
import android.text.method.TextKeyListener;

import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends Activity implements OnNavigationListener {
	public static DialogFragment sFavouritesDialog;
	
	private AQuery aq;
	private AutoCompleteTextView mFromText;
	private AutoCompleteTextView mToText;

	private LocationManager mLocationManager;
	private SharedPreferences mPreferences;
	
	private int mSelectedCityIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize class variables
		aq = new AQuery(this);
		aq.hardwareAccelerated11();
		mFromText = (AutoCompleteTextView) findViewById(R.id.fromAddressTextView);
		mToText = (AutoCompleteTextView) findViewById(R.id.toAddressTextView);
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Set actionBar to drop-down mode
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);

		// Open setting for selected city reading/writing
		mPreferences = getSharedPreferences(Constants.APP_PREFS, MODE_PRIVATE);

		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(
				actionBar.getThemedContext(), 
				R.array.matkahuoltoCities,
				android.R.layout.simple_spinner_dropdown_item
		);

		actionBar.setListNavigationCallbacks(spinnerAdapter, this);
		mSelectedCityIndex = mPreferences.getInt("selectedCity", 0);
		actionBar.setSelectedNavigationItem(mSelectedCityIndex);

		// Fetch and parse token
		fetchToken();

		// Enable dictionary suggestions
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
		mFromText.setKeyListener(input);
		mToText.setKeyListener(input);

		// Bind address search string change listeners
		mFromText.addTextChangedListener(new AddressChangeListener(this, mFromText, mSelectedCityIndex));
		mToText.addTextChangedListener(new AddressChangeListener(this, mToText, mSelectedCityIndex));

		// Bind address suggestion click listeners
		mFromText.setOnItemClickListener(new AddressClickListener(mFromText));
		mToText.setOnItemClickListener(new AddressClickListener(mToText));

		((SeekBar)findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				aq.id(R.id.editText5).text(Integer.toString(progress+1));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Ignore
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Ignore
			}
		});

		// Fill date and time edit texts with "now" values
		final Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH);
		int year = c.get(Calendar.YEAR);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		aq.id(R.id.dateText).text(KlmobiUtils.niceDateFormat(day, month, year));
		aq.id(R.id.editText4).text(KlmobiUtils.niceTimeFormat(hour, minute));

		// Set default selections to extra options
		aq.id(R.id.spinner1).setSelection(0);
		aq.id(R.id.spinner2).setSelection(1);
		aq.id(R.id.spinner3).setSelection(3);
		aq.id(R.id.spinner4).setSelection(3);

		// Focus to fromText
		mFromText.requestFocus();

	}

	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		mSelectedCityIndex = position;

		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt("selectedCity", position);
		editor.commit();

		AddressChangeListener.refreshUrl(position);

		GlobalApp.startAddress = null;
		GlobalApp.endAddress = null;
		mFromText.setText("");
		mToText.setText("");

		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.reverse_settings:
				switchRouteDirection();
				return true;

			case R.id.search_settings:
				onSearchButtonClicked();
				return true;

			case R.id.update_token_settings:
				fetchToken();
				return true;

			case R.id.locate_settings:
				locateAddressToFocus();
				return true;

			case R.id.about_settings:
				showAboutDialog();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void onSearchButtonClicked() {
		if (GlobalApp.startAddress == null || GlobalApp.endAddress == null) {
			Toast.makeText(this, getString(R.string.fromToNotSet), Toast.LENGTH_LONG).show();
			return;
		}

		aq.ajaxCancel();

		// Oh god this ugliness..
		String[] dateComponents = aq.id(R.id.dateText).getText().toString().split("/");
		String date = dateComponents[2] + dateComponents[1] + dateComponents[0];
		String time = aq.id(R.id.editText4).getText().toString().replace(":", "");
		String numberOfRoutes = aq.id(R.id.editText5).getText().toString();

		String routeType;
		int routeTypeIndex = aq.id(R.id.spinner1).getSelectedItemPosition();
		switch (routeTypeIndex) {
			case 1:
				routeType = "fastest"; 
				break;
			case 2:
				routeType = "minchanges";
				break;
			case 3:
				routeType = "minwalk";
				break;
			default:
				routeType = "default";
				break;
		}

		String walkingSpeed = ((String)(aq.id(R.id.spinner2).getSelectedItem())).split(" ")[1];
		String maxWalkingDistance = ((String)(aq.id(R.id.spinner3).getSelectedItem())).split(" ")[0];
		String changeMargin = ((String)(aq.id(R.id.spinner4).getSelectedItem())).split(" ")[0];

		Intent intent = new Intent(this, RouteResultsActivity.class);
		intent.putExtra(Constants.EXTRA_CITY_INDEX, mSelectedCityIndex);
		intent.putExtra(Constants.EXTRA_DATE, date);
		intent.putExtra(Constants.EXTRA_TIME, time);
		intent.putExtra(Constants.EXTRA_NUMBER_OF_ROUTES, numberOfRoutes);
		intent.putExtra(Constants.EXTRA_ROUTE_TYPE, routeType);
		intent.putExtra(Constants.EXTRA_WALKING_SPEED, walkingSpeed);
		intent.putExtra(Constants.EXTRA_MAX_WALKING_DISTANCE, maxWalkingDistance);
		intent.putExtra(Constants.EXTRA_CHANGE_MARGIN, changeMargin);
		startActivity(intent);
	}

	public void pickTime(View view) {
		DialogFragment timePicker = TimePickerFragment.getInstance(R.id.editText4);
		timePicker.show(getFragmentManager(), "timePicker");
	}

	public void pickDate(View view) {
		DialogFragment datePicker = DatePickerFragment.getInstance(R.id.dateText);
		datePicker.show(getFragmentManager(), "datePicker");
	}

	public void clearAddressSearch(View v) {
		int viewId = v.getId();
		if (viewId == R.id.fromClearBtn) {
			mFromText.setText("");
			GlobalApp.startAddress = null;
		}
		else if (viewId == R.id.toClearBtn) {
			mToText.setText("");
			GlobalApp.endAddress = null;
		}
	}

	public void onFavouriteButtonClicked(View v) {
		int targetView;
		if (v.getId() == R.id.fromFavBtn)
			targetView = R.id.fromAddressTextView;
		else
			targetView = R.id.toAddressTextView;

		sFavouritesDialog = new FavouritesDialogFragment();
		Bundle fragmentArgs = new Bundle();
		fragmentArgs.putInt("targetView", targetView);
		sFavouritesDialog.setArguments(fragmentArgs);

		sFavouritesDialog.show(getFragmentManager(), "favourites");
	}

	public void toggleExtraOptions(View v) {
		RelativeLayout extraOptionsLayout = (RelativeLayout) findViewById(R.id.extraOptionsLayout);
		if (extraOptionsLayout.getVisibility() == View.GONE) {
			extraOptionsLayout.setVisibility(View.VISIBLE);
			aq.id(R.id.advancedBtn).getButton().setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_action_collapse, 0, 0);
		} else {
			extraOptionsLayout.setVisibility(View.GONE);
			aq.id(R.id.advancedBtn).getButton().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_action_expand);	
		}
	}

	private void showAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String versionName = "???";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException nnfex) {
			// ignore
		}
		String message = "MH-Paikalliset\nversion " + versionName +"\n\n"
				+ "Mikko Oksa \u00a9 " + Calendar.getInstance().get(Calendar.YEAR) + "\nmikkoks@cs.uef.fi\n\n"
				+ "AQuery\nhttp://code.google.com/p/android-query/\n\n"
				+ "jsoup\nhttp://jsoup.org/\n\n"
				+ "CoordinateUtils\nhttps://github.com/Sandmania/CoordinateUtils";
		AlertDialog aboutDialog = builder.setMessage(message)
				.setTitle(R.string.aboutTitle)
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Just close..
					}
				})	       
				.create();
		aboutDialog.show();

		TextView messageView = (TextView) aboutDialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);
	}

	private void switchRouteDirection() {
		Editable tmp_editable = mFromText.getText();

		mFromText.setText(mToText.getText());
		mFromText.dismissDropDown();

		mToText.setText(tmp_editable);
		mToText.dismissDropDown();

		Address tmp_address = GlobalApp.startAddress;
		GlobalApp.startAddress = GlobalApp.endAddress;
		GlobalApp.endAddress = tmp_address;
	}

	private void locateAddressToFocus() {
		boolean gpsLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean networkLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (!gpsLocationEnabled && !networkLocationEnabled) {
			Toast.makeText(this, getString(R.string.noLocationingAvailable), Toast.LENGTH_LONG).show();
			return;
		}
		
		String provider = "";
		if (gpsLocationEnabled) provider += "GPS";
		if (networkLocationEnabled) {
			if (provider.length() != 0) {
				provider += " + ";
			}
			provider += getString(R.string.network);
		}
		LocationToFocusListener listener = new LocationToFocusListener(
				mLocationManager,
				new Geocoder(this),
				(mFromText.hasFocus() ? mFromText : mToText),
				75, 
				gpsLocationEnabled ? 30 : 5,
				provider
		);
		if (networkLocationEnabled) {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
		}
		if (gpsLocationEnabled) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
		}
	}

	private void fetchToken() {
		aq.ajax("http://" + Constants.citySubdomains[mSelectedCityIndex] + ".matkahuolto.info/fi/config.js.php", 
				String.class, 
				new AjaxCallback<String>() {

			@Override
			public void callback(String url, String html, AjaxStatus status) {
				try {
					JSONObject config = new JSONObject(html.split("=")[1].replace(";", ""));
					GlobalApp.token = config.getString("token");
					Toast.makeText(MainActivity.this, getString(R.string.tokenOk), Toast.LENGTH_SHORT).show();
				} catch (JSONException jsonex) {
					Toast.makeText(MainActivity.this, "Matkahuolto-token VIRHE" , Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}