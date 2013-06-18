package fi.metacity.klmobi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.LongClick;
import com.googlecode.androidannotations.annotations.NonConfigurationInstance;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.SeekBarProgressChange;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.TextChange;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import fi.metacity.klmobi.AddressLocationListener.OnLocationFoundListener;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements OnNavigationListener, 
		OnTimeSetListener, OnDateSetListener {

	@NonConfigurationInstance
	Calendar mDateTime = GregorianCalendar.getInstance();

	@App
	MHApp mGlobals;

	@Pref
	Preferences_ mPreferences;
	
	@SystemService
	LocationManager mLocationManager;

	@ViewById(R.id.startText)
	AutoCompleteTextView mStartText;

	@ViewById(R.id.endText)
	AutoCompleteTextView mEndText;

	@ViewById(R.id.startClearBtn)
	ImageButton mStartClearButton;

	@ViewById(R.id.endClearBtn)
	ImageButton mEndClearButton;

	@ViewById(R.id.timeText)
	TextView mTimeText;

	@ViewById(R.id.dateText)
	TextView mDateText;
	
	@ViewById(R.id.toggleAdvancedBtn)
	ImageButton mToggleAdvancedOptionsButton;
	
	@ViewById(R.id.advancedOptionsLayout)
	RelativeLayout mAdvancedOptionsLayout;
	
	@ViewById(R.id.numberOfRoutes)
	TextView mNumberOfRoutes;
	
	@ViewById(R.id.numberOfRoutesSeekBar)
	SeekBar mNumberOfRoutesSeekBar;
	
	@ViewById(R.id.walkingSpeedSpinner)
	Spinner mWalkingSpeedSpinner;
	
	@ViewById(R.id.maxWalkingDistanceSpinner)
	Spinner mMaxWalkingSpeedSpinner;
	
	@ViewById(R.id.routeTypeSpinner)
	Spinner mRouteTypeSpinner;
	
	@ViewById(R.id.changeMarginsSpinner)
	Spinner mChangeMarginsSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mGlobals.getToken().length() == 0) {
			fetchToken();
		}
		
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);

		ArrayAdapter<CharSequence> citiesAdapter = ArrayAdapter.createFromResource(
				actionBar.getThemedContext(), 
				R.array.matkahuoltoCities,
				android.R.layout.simple_spinner_dropdown_item
				);
		actionBar.setListNavigationCallbacks(citiesAdapter, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		String newBaseUrl = "http://" + Constants.CITY_SUBDOMAINS[position] + ".matkahuolto.info/";
		mPreferences.baseUrl().put(newBaseUrl);

		mPreferences.selectedCityIndex().put(position);
		return true;
	}
	
	@AfterViews
	public void initialize() {
		// Set saved city
		getActionBar().setSelectedNavigationItem(mPreferences.selectedCityIndex().get());
		
		// Set current date & time
		setDateTimeTexts(mDateTime);
		
		// Set address select handlers
		mStartText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Address address = (Address) mStartText.getAdapter().getItem(position);
				mGlobals.setStartAddress(address);
				mStartText.dismissDropDown();
			}
		});
		mEndText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Address address = (Address) mEndText.getAdapter().getItem(position);
				mGlobals.setEndAddress(address);
				mEndText.dismissDropDown();
			}
		});
		
		// Set default advanced option values
		mNumberOfRoutesSeekBar.setProgress(4);
		mWalkingSpeedSpinner.setSelection(1);
		mMaxWalkingSpeedSpinner.setSelection(3);
		mChangeMarginsSpinner.setSelection(3);
		
		// Enable dictionary suggestions
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
		mStartText.setKeyListener(input);
		mEndText.setKeyListener(input);
		
		mStartText.requestFocus();
	}

	@TextChange({R.id.startText, R.id.endText})
	public void onTextChangesOnSomeTextViews(TextView addressInput, CharSequence text) {
		ImageButton clearBtnToShowOrHide = (addressInput == mStartText) ? mStartClearButton : mEndClearButton;
		if (text.toString().trim().length() != 0) {
			clearBtnToShowOrHide.setVisibility(View.VISIBLE);
			searchAddresses(addressInput, text);		
		} else {
			((AutoCompleteTextView) addressInput).dismissDropDown();
			clearBtnToShowOrHide.setVisibility(View.INVISIBLE);
		}
	}

	@Click(R.id.startClearBtn)
	public void clearStart() {
		mStartText.setText("");
		mGlobals.setStartAddress(null);
	}

	@Click(R.id.endClearBtn)
	public void clearEnd() {
		mEndText.setText("");
		mGlobals.setEndAddress(null);
	}

	@Click(R.id.startFavouritesBtn)
	public void showStartFavourites() {
		showFavouriteDialog(new FavouritesDialog.OnFavouriteSelectedListener() {
			@Override
			public void onFavouriteSelected(Address selectedAddress) {
				mGlobals.setStartAddress(selectedAddress);
				mStartText.setText(selectedAddress.toString());
				mStartText.dismissDropDown();
			}
		}, "startFavourites", mGlobals.getStartAddress());
	}

	@Click(R.id.endFavouritesBtn)
	public void showEndFavourites() {
		showFavouriteDialog(new FavouritesDialog.OnFavouriteSelectedListener() {
			@Override
			public void onFavouriteSelected(Address selectedAddress) {
				mGlobals.setEndAddress(selectedAddress);
				mEndText.setText(selectedAddress.toString());
				mEndText.dismissDropDown();
			}
		}, "endFavourites", mGlobals.getEndAddress());
	}
	
	private void showFavouriteDialog(FavouritesDialog.OnFavouriteSelectedListener selectedListener, 
			String tag, Address savableAddress) {
		
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(tag);
		if (prev != null) {
			transaction.remove(prev);
		}
		transaction.addToBackStack(null);
		
		FavouritesDialog dialog = FavouritesDialog_.builder()
				.mSaveableFavourite((savableAddress == null) ? "" : savableAddress.json.toString()).build();
		dialog.setSelectedListener(selectedListener);
		dialog.show(transaction, tag);
	}
	
	@Click(R.id.startLocateBtn)
	public void locateStart() {
		OnLocationFoundListener listener = new OnLocationFoundListener() {
			@Override
			public void onLocationFound(Location location) {
				Address address = Utils.locationToCoordinateAddress(location);
				if (address != null) {
					mGlobals.setStartAddress(address);
					mStartText.setText(address.toString());
					mStartText.dismissDropDown();
				}
			}
		};
		startLocating(listener);
	}
	
	@Click(R.id.endLocateBtn)
	public void locateEnd() {
		OnLocationFoundListener listener = new OnLocationFoundListener() {
			@Override
			public void onLocationFound(Location location) {
				Address address = Utils.locationToCoordinateAddress(location);
				if (address != null) {
					mGlobals.setEndAddress(address);
					mEndText.setText(address.toString());
					mEndText.dismissDropDown();
				}
			}
		};
		startLocating(listener);
	}
	
	private void startLocating(OnLocationFoundListener foundListener) {
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
		AddressLocationListener locationListener = new AddressLocationListener(
				this,
				75, 
				gpsLocationEnabled ? 30 : 5,
				provider,
				foundListener
		);
		if (networkLocationEnabled) {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		}
		if (gpsLocationEnabled) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		}
	}

	@Click(R.id.swapBtn)
	public void swapAddresses() {
		// Swap texts
		CharSequence tmpText = mStartText.getText();
		mStartText.setText(mEndText.getText());
		mEndText.setText(tmpText);

		mStartText.dismissDropDown();
		mEndText.dismissDropDown();

		// Swap Address objects
		Address tmpAddress = mGlobals.getStartAddress();
		mGlobals.setStartAddress(mGlobals.getEndAddress());
		mGlobals.setEndAddress(tmpAddress);
	}

	@LongClick(R.id.swapBtn)
	public void showSwapHint() {
		Toast.makeText(this, getString(R.string.swapStartAndEnd), Toast.LENGTH_LONG).show();
	}
	
	@Click(R.id.toggleAdvancedBtn)
	public void toggleAdvancedOptions() {
		if (mAdvancedOptionsLayout.getVisibility() == View.GONE) { // Show if hidden
			mAdvancedOptionsLayout.setVisibility(View.VISIBLE);
			mToggleAdvancedOptionsButton.setImageResource(R.drawable.ic_menu_earlier);
		} else {  // Hide if shown
			mAdvancedOptionsLayout.setVisibility(View.GONE);
			mToggleAdvancedOptionsButton.setImageResource(R.drawable.ic_menu_later);
		}
	}

	@LongClick(R.id.toggleAdvancedBtn)
	public void toggleAdvancedOptionsHint() {
		Toast.makeText(this, getString(R.string.showAdvancedOptions), Toast.LENGTH_LONG).show();
	}

	@Click(R.id.findRoutesBtn)
	public void findRoutes() {
		if (mGlobals.getStartAddress() == null) {
			Toast.makeText(this, "\"" + getString(R.string.from) + "\" " + getString(R.string.notSetProperly), 
					Toast.LENGTH_SHORT).show();
		} else if (mGlobals.getEndAddress() == null) { 
			Toast.makeText(this, "\"" + getString(R.string.to) + "\" " + getString(R.string.notSetProperly), 
					Toast.LENGTH_SHORT).show();
		} else {
			mGlobals.getRoutes().clear();
			Intent intent = new Intent(this, RoutesActivity_.class);
			intent.putExtra(Constants.EXTRA_DATE, String.format("%d%02d%02d", mDateTime.get(Calendar.YEAR), 
					mDateTime.get(Calendar.MONTH) + 1, mDateTime.get(Calendar.DAY_OF_MONTH)));
			intent.putExtra(Constants.EXTRA_TIME, String.format("%02d%02d", mDateTime.get(Calendar.HOUR_OF_DAY), 
					mDateTime.get(Calendar.MINUTE)));
			intent.putExtra(Constants.EXTRA_NUMBER_OF_ROUTES, mNumberOfRoutes.getText().toString());
			intent.putExtra(Constants.EXTRA_ROUTING_TYPE, 
					Constants.ROUTING_TYPES[mRouteTypeSpinner.getSelectedItemPosition()]);
			intent.putExtra(Constants.EXTRA_WALKING_SPEED, 
					((String)(mWalkingSpeedSpinner.getSelectedItem())).split(" ")[1]);
			intent.putExtra(Constants.EXTRA_MAX_WALKING_DISTANCE, 
					((String)(mMaxWalkingSpeedSpinner.getSelectedItem())).split(" ")[0]);
			intent.putExtra(Constants.EXTRA_CHANGE_MARGIN, 
					((String)(mChangeMarginsSpinner.getSelectedItem())).split(" ")[0]);
			startActivity(intent);
		}
	}

	@Click(R.id.timeText)
	public void pickTime() {
		TimePickerDialog timePicker = new TimePickerDialog(
				this, 
				this, 
				mDateTime.get(Calendar.HOUR_OF_DAY), 
				mDateTime.get(Calendar.MINUTE), 
				true
				);
		timePicker.show();
	}

	@Click(R.id.dateText)
	public void pickDate() {
		DatePickerDialog datePicker = new DatePickerDialog(
				this, 
				this, 
				mDateTime.get(Calendar.YEAR), 
				mDateTime.get(Calendar.MONTH),
				mDateTime.get(Calendar.DAY_OF_MONTH)
				);
		datePicker.show();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
		mDateTime.set(Calendar.MINUTE, minute);
		setDateTimeTexts(mDateTime);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		mDateTime.set(Calendar.YEAR, year);
		mDateTime.set(Calendar.MONTH, monthOfYear);
		mDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		setDateTimeTexts(mDateTime);
	}

	private void setDateTimeTexts(Calendar dateTime) {
		String time = String.format(Locale.US, "%02d:%02d", dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE));
		String date = dateTime.get(Calendar.DAY_OF_MONTH)+ "." 
				+ (dateTime.get(Calendar.MONTH) + 1) + "."     // Months in Calendar class are 0-11
				+ dateTime.get(Calendar.YEAR);

		mTimeText.setText(time);
		mDateText.setText(date);
	}
	
	@SeekBarProgressChange(R.id.numberOfRoutesSeekBar)
	public void onNumberOfRoutesChanged(SeekBar seekBar, int progress) {
		mNumberOfRoutes.setText(String.valueOf(progress + 1));
	}
	
	@OptionsItem(R.id.about_settings)
	public void showAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String versionName = "???";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException nnfex) {
			// ignore
		}
		String message = "MH-Paikalliset\nv. " + versionName +"\n\n"
				+ "Mikko Oksa \u00a9 " + Calendar.getInstance().get(Calendar.YEAR) + "\nmikkoks@cs.uef.fi\n\n"
				+ "AndroidAnnotations\nhttp://androidannotations.org/\n\n"
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
	
	@OptionsItem(R.id.third_party_licenses)
	public void showThirdPartyLicenses() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = "<b>AndroidAnnotations</b><br>" + Utils.getAndroidAnnotationsLicense()
				+ "<br><br><b>jsoup</b><br>" + Utils.getJsoupLicense()
				+ "<br><br><b>CoordinateUtils</b><br>" + Utils.getCoordinateUtilsLicense();
		AlertDialog aboutDialog = builder.setMessage(Html.fromHtml(message))
				.setTitle(R.string.thirdPartyLicenses)
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Just close..
					}
				})	       
				.create();
		
		aboutDialog.show();
	}
	
	@OptionsItem(R.id.update_token)
	public void updateToken() {
		fetchToken();
	}

	@Background
	public void searchAddresses(TextView addressInput, CharSequence text) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("language", "fi");
		params.put("maxresults", "30");
		params.put("token", mGlobals.getToken());
		params.put("key", text.toString());

		try {
			String response = Utils.httpPost(mPreferences.baseUrl().get() + "geocode.php", params);
			JSONObject json = new JSONObject(response);
			if (json.getInt("status") == 0) {
				List<Address> locations = new ArrayList<Address>();

				JSONArray locationsJson = json.getJSONObject("data").getJSONArray("locations");
				for (int i = 0, len = locationsJson.length(); i < len; ++i) {
					locations.add(new Address(locationsJson.getJSONObject(i)));
				}
				setAddressAdapter(addressInput, locations);
			} else {
				((AutoCompleteTextView) addressInput).dismissDropDown();
			}
		} catch (IOException ioex) {
			Log.d("searchAddresses", ioex.toString());
			ioex.printStackTrace();
		} catch (JSONException jsonex) {
			Log.d("searchAddresses", jsonex.toString());
		}
	}

	@UiThread
	public void setAddressAdapter(TextView tv, List<Address> locations) {
		// Always make new ArrayAdapter object, won't update suggestions correctly otherwise!
		ArrayAdapter<Address> locationsAdapter = new ArrayAdapter<Address>(
				this, 
				android.R.layout.simple_spinner_dropdown_item, 
				locations
				);
		synchronized (tv) {
			((AutoCompleteTextView) tv).setAdapter(locationsAdapter);
			((AutoCompleteTextView) tv).showDropDown();
		}
		locationsAdapter.notifyDataSetChanged();
	}

	@Background
	public void fetchToken() {
		for (int i = 0; i < 2 && mGlobals.getToken().length() == 0; ++i) { // Attempt twice
			try {
				String response = Utils.httpGet(mPreferences.baseUrl().get() + "fi/config.js.php");
				JSONObject config = new JSONObject(response.split("=")[1].replace(";", ""));
				String token = config.getString("token");
				mGlobals.setToken(token);
				showToast("Matkahuolto-token OK!", Toast.LENGTH_SHORT);
			} catch (IOException ioex) {
				// Ignore
			} catch (JSONException jsonex) {
				// Ignore
			}
		}
		
		if (mGlobals.getToken().length() == 0) {
			showToast(getString(R.string.gettingTokenFailedToast), Toast.LENGTH_LONG);
		}
	}

	@UiThread
	public void showToast(String text, int duration) {
		Toast.makeText(this, text, duration).show();
	}

}
