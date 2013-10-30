package fi.metacity.klmobi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.LongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.SeekBarProgressChange;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import fi.metacity.klmobi.AddressLocationListener.OnLocationFoundListener;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements OnNavigationListener, 
		OnTimeSetListener, OnDateSetListener {

	@App
	MHApp mGlobals;

	@Pref
	Preferences_ mPreferences;
	
	@SystemService
	LocationManager mLocationManager;
	
	@SystemService 
	ConnectivityManager mConnectivityManager;

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
	Button mToggleAdvancedOptionsButton;
	
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
	
	@ViewById(R.id.departureArrivalSwitch)
	Switch mDepartureArrivalSwitch;
	
	private ProgressDialog mTokenDownloadingDialog;

	private final Calendar mDateTime = GregorianCalendar.getInstance();
	private final List<Address> mAddressResults = new ArrayList<Address>();
	private final AtomicInteger mAddressRequestId = new AtomicInteger();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		if (!isNetworkAvailable()) {
			new AlertDialog.Builder(this).setMessage(R.string.networkNotAvailable).setPositiveButton("OK", null).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		fetchTokenIfNeeded(false);
		
		Address start = mGlobals.getStartAddress();
		Address end = mGlobals.getEndAddress();
		mStartText.setText(start != null ? start.toString() : "");
		mStartText.dismissDropDown();
		mEndText.setText(end != null ? end.toString() : "");
		mEndText.dismissDropDown();
		
		// Clear the downloaded data (would be replaced anyway if "FIND ROUTES" was pressed)
		mGlobals.setDetailsXmlString("");
		mGlobals.setTurkuMapQueryString("");
		mGlobals.getRoutes().clear();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		String newBaseUrl = "";
		if (position == 20) { // TURKU
			newBaseUrl = Constants.TURKU_BASE_URL;
		} else {
			newBaseUrl = "http://" + Constants.CITY_SUBDOMAINS[position] + ".matkahuolto.info/";
		}
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
				Address address = mAddressResults.get(position);
				mGlobals.setStartAddress(address);
				mStartText.dismissDropDown();
			}
		});
		mEndText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Address address = mAddressResults.get(position);
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

	@AfterTextChange({R.id.startText, R.id.endText})
	public void onAddressSearchStringChanged(TextView addressInput, Editable text) {
		ImageButton clearBtnToShowOrHide = (addressInput == mStartText) ? mStartClearButton : mEndClearButton;
		if (text.toString().trim().length() != 0) {
			clearBtnToShowOrHide.setVisibility(View.VISIBLE);
			ArrayAdapter<String> loadingTextAdapter = new ArrayAdapter<String>(
					this, 
					android.R.layout.simple_list_item_1, 
					new String[] { getString(R.string.loadingText) }
					);
			((AutoCompleteTextView) addressInput).setAdapter(loadingTextAdapter);
			searchAddresses(addressInput, text, mAddressRequestId.incrementAndGet());		
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
	
	@Click({R.id.startOverflowBtn, R.id.endOverflowBtn})
	public void showAddressOverflowMenu(final View view) {
		PopupMenu popup = new PopupMenu(this, view);
		popup.inflate(R.menu.address_overflow);
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					
					case R.id.overflow_favourites:
						if (view.getId() == R.id.startOverflowBtn) {
							startFromFavourites();
						} else {
							endFromFavourites();
						}
						return true;
					
					case R.id.overflow_locate:
						if (view.getId() == R.id.startOverflowBtn) {
							locateStart();
						} else {
							locateEnd();
						}
						return true;
						
					case R.id.overflow_history:
						if (view.getId() == R.id.startOverflowBtn) {
							startFromHistory();
						} else {
							endFromHistory();
						}
						return true;
					
					default:
						return false;	
				}
			}
		});
		popup.show();
	}

	private void startFromFavourites() {
		showFavouriteDialog(new FavouritesDialog.OnFavouriteSelectedListener() {
			@Override
			public void onFavouriteSelected(Address selectedAddress) {
				mGlobals.setStartAddress(selectedAddress);
				mStartText.setText(selectedAddress.toString());
				mStartText.dismissDropDown();
			}
		}, "startFavourites", mGlobals.getStartAddress());
	}

	private void endFromFavourites() {
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
	
	private void startFromHistory() {
		showHistoryDialog(new HistoryDialog.OnHistorySelectedListener() {
			@Override
			public void onHistorySelected(Address selectedAddress) {
				mGlobals.setStartAddress(selectedAddress);
				mStartText.setText(selectedAddress.toString());
				mStartText.dismissDropDown();
			}
		}, "startHistory");
	}

	private void endFromHistory() {
		showHistoryDialog(new HistoryDialog.OnHistorySelectedListener() {
			@Override
			public void onHistorySelected(Address selectedAddress) {
				mGlobals.setEndAddress(selectedAddress);
				mEndText.setText(selectedAddress.toString());
				mEndText.dismissDropDown();
			}
		}, "endFavourites");
	}
	
	private void showHistoryDialog(HistoryDialog.OnHistorySelectedListener selectedListener, String tag) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(tag);
		if (prev != null) {
			transaction.remove(prev);
		}
		transaction.addToBackStack(null);
		
		HistoryDialog dialog = new HistoryDialog_();
		dialog.setSelectedListener(selectedListener);
		dialog.show(transaction, tag);
	}
	
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

	@OptionsItem(R.id.swap_menu_item)
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
	
	@Click(R.id.toggleAdvancedBtn)
	public void toggleAdvancedOptions() {
		if (mAdvancedOptionsLayout.getVisibility() == View.GONE) { // Show if hidden
			mAdvancedOptionsLayout.setVisibility(View.VISIBLE);
			mToggleAdvancedOptionsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collapse, 0, 0);
		} else {  // Hide if shown
			mAdvancedOptionsLayout.setVisibility(View.GONE);
			mToggleAdvancedOptionsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_expand);	
		}
	}
	
	@Click(R.id.resetDatetimeBtn) 
	public void resetDatetime() {
		mDateTime.setTime(Calendar.getInstance().getTime());
		setDateTimeTexts(mDateTime);
	}
	
	@LongClick(R.id.resetDatetimeBtn) 
	public void showResetDatetimeToast() {
		Toast.makeText(this, getString(R.string.setToNow), Toast.LENGTH_SHORT).show();
	}

	@OptionsItem(R.id.find_routes_menu_item)
	public void findRoutes() {
		if (mGlobals.getStartAddress() == null) {
			Toast.makeText(this, "\"" + getString(R.string.from) + "\" " + getString(R.string.notSetProperly), 
					Toast.LENGTH_SHORT).show();
		} else if (mGlobals.getEndAddress() == null) { 
			Toast.makeText(this, "\"" + getString(R.string.to) + "\" " + getString(R.string.notSetProperly), 
					Toast.LENGTH_SHORT).show();
		} else {
			
			// Put to history
			try {
				Address start = mGlobals.getStartAddress();
				Address end = mGlobals.getEndAddress();
				
				JSONArray newHistory = new JSONArray();
				newHistory.put(start.json);
				newHistory.put(end.json);
				JSONArray historyJson = new JSONArray(mPreferences.addressHistory().get());
				for (int i = 0; i < historyJson.length(); i++) {
					Address historyAddress = new Address(historyJson.getJSONObject(i));
					String coordinates = historyAddress.coordinatesOnly();
					if (newHistory.length() < 10 
					 && !coordinates.equals(start.coordinatesOnly()) 
					 && !coordinates.equals(end.coordinatesOnly())) {
						newHistory.put(historyAddress.json);
					}
				}
				mPreferences.addressHistory().put(newHistory.toString());
			} catch (JSONException jsonex) {
				Log.e("MainActivity", jsonex.toString());
			}
			
			
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
			intent.putExtra(Constants.EXTRA_TIME_DIRECTION, 
					mDepartureArrivalSwitch.isChecked() ? "backward" : "forward");
			
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
		
		Calendar now = Calendar.getInstance();
		String date = "";
		if (now.get(Calendar.DAY_OF_YEAR) == dateTime.get(Calendar.DAY_OF_YEAR)
		 && now.get(Calendar.YEAR) == dateTime.get(Calendar.YEAR)) {
			
			date = getString(R.string.today);
		} else {
			now.add(Calendar.DAY_OF_YEAR, 1);
			if (now.get(Calendar.DAY_OF_YEAR) == dateTime.get(Calendar.DAY_OF_YEAR)
			 && now.get(Calendar.YEAR) == dateTime.get(Calendar.YEAR)) {
				
				date = getString(R.string.tomorrow);
			} else {
				date = dateTime.get(Calendar.DAY_OF_MONTH) + "." 
						+ (dateTime.get(Calendar.MONTH) + 1) + "."     // Months in Calendar class are 0-11
						+ dateTime.get(Calendar.YEAR);
			}
		}
		
		mTimeText.setText(time);
		mDateText.setText(date);
	}
	
	@SeekBarProgressChange(R.id.numberOfRoutesSeekBar)
	public void onNumberOfRoutesChanged(SeekBar seekBar, int progress) {
		mNumberOfRoutes.setText(String.valueOf(progress + 1));
	}
	
	@OptionsItem(R.id.about_menu_item)
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
				+ "CoordinateUtils\nhttps://github.com/Sandmania/CoordinateUtils\n\n"
				+ "Pager Sliding TabStrip\nhttps://github.com/astuetz/PagerSlidingTabStrip\n\n"
				+ "Picasso\nhttp://square.github.io/picasso";
		AlertDialog aboutDialog = builder.setMessage(message)
				.setTitle(R.string.aboutTitle)
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("OK", null)  
				.create();
		aboutDialog.show();

		TextView messageView = (TextView) aboutDialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);
	}
	
	@OptionsItem(R.id.clear_history_menu_item)
	public void clearHistory() {
		mPreferences.addressHistory().put("[]");
		Toast.makeText(this, getString(R.string.historyCleared), Toast.LENGTH_SHORT).show();
	}
	
	@OptionsItem(R.id.third_party_licenses_menu_item)
	public void showThirdPartyLicenses() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = "<b>AndroidAnnotations</b><br>" + Utils.getAndroidAnnotationsLicense()
				+ "<br><br><b>jsoup</b><br>" + Utils.getJsoupLicense()
				+ "<br><br><b>CoordinateUtils</b><br>" + Utils.getCoordinateUtilsLicense()
				+ "<br><br><b>Pager Sliding TabStrip</b><br>" + Utils.getPagerSlidingTabStripLicense()
				+ "<br><br><b>Picasso</b><br>" + Utils.getPicassoLicense();
		AlertDialog aboutDialog = builder.setMessage(Html.fromHtml(message))
				.setTitle(R.string.thirdPartyLicenses)
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("OK", null)	       
				.create();
		
		aboutDialog.show();
	}
	
	@OptionsItem(R.id.update_token_menu_item)
	public void updateToken() {
		fetchTokenIfNeeded(true);
	}

	@Background
	public void searchAddresses(TextView addressInput, CharSequence text, int requestId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("language", "fi");
		params.put("maxresults", "30");
		params.put("token", mPreferences.token().get());
		params.put("key", text.toString());

		try {
			String response = Utils.httpPost(mPreferences.baseUrl().get() + "geocode.php", params);
			setAddressAdapter(addressInput, response, requestId);
		} catch (IOException ioex) {
			Log.d("searchAddresses", ioex.toString());
			ioex.printStackTrace();
		}
	}

	@UiThread
	public void setAddressAdapter(TextView tv, String jsonResponse, int requestId) {
		if (mAddressRequestId.get() == requestId) {
			try {
				JSONObject json = new JSONObject(jsonResponse);
				if (json.getInt("status") == 0) {
					mAddressResults.clear();
					JSONArray locationsJson = json.getJSONObject("data").getJSONArray("locations");
					for (int i = 0, len = locationsJson.length(); i < len; ++i) {
						mAddressResults.add(new Address(locationsJson.getJSONObject(i)));
					}
					// Always make new ArrayAdapter object, won't update suggestions correctly otherwise!
					ArrayAdapter<Address> locationsAdapter = new ArrayAdapter<Address>(
							this, 
							android.R.layout.simple_list_item_1, 
							mAddressResults
							);
					((AutoCompleteTextView) tv).setAdapter(locationsAdapter);
					locationsAdapter.notifyDataSetChanged();
				} else {
					((AutoCompleteTextView) tv).dismissDropDown();
				}
			} catch (JSONException jsonex) {
				Log.d("searchAddresses", jsonex.toString());
			}
		}
	}

	@Background
	public void fetchTokenIfNeeded(boolean force) {
		Calendar now = Calendar.getInstance();
		Calendar tokenLastSyncTime = Calendar.getInstance();
		tokenLastSyncTime.setTimeInMillis(mPreferences.tokenLastSyncTime().get());
		if (now.get(Calendar.YEAR) > tokenLastSyncTime.get(Calendar.YEAR)
		 || now.get(Calendar.DAY_OF_YEAR) > tokenLastSyncTime.get(Calendar.DAY_OF_YEAR)
		 || force) {
			dismissTokenDownloadDialog();
			showTokenDownloadDialog();
			try {
				String mainpage = Utils.httpGet(mPreferences.baseUrl().get());
				Document doc = Jsoup.parse(mainpage);
				String hash = doc.select("link[href^=/css]").first().attr("href").split("_")[1].split("\\.")[0];
				
				String url = "";
				if (mPreferences.selectedCityIndex().get() == 20) { // If TURKU
					url = mPreferences.baseUrl().get() + "fi/config.js_" + hash + ".php";
				} else {
					url = mPreferences.baseUrl().get() + "fi/config_" + hash + ".js.php";
				}
				String response = Utils.httpGet(url);
				JSONObject config = new JSONObject(response.split("=")[1].replace(";", ""));
				String token = config.getString("token");
				mPreferences.token().put(token);
				mPreferences.tokenLastSyncTime().put(System.currentTimeMillis());
				showToast(getString(R.string.tokenOk), Toast.LENGTH_SHORT);
			} catch (Exception ex) {
				showToast(getString(R.string.gettingTokenFailedToast), Toast.LENGTH_LONG);
			} finally {
				dismissTokenDownloadDialog();
			}
		}
	}

	@UiThread
	public void showTokenDownloadDialog() {
		mTokenDownloadingDialog = ProgressDialog.show(
				this, 
				getString(R.string.progressDialogTitle), 
				getString(R.string.updatingToken), 
				true, 
				true,
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(MainActivity.this, getString(R.string.gettingTokenFailedToast), Toast.LENGTH_LONG).show();
					}
				}
				);
	}
	
	@UiThread
	public void dismissTokenDownloadDialog() {
		if (mTokenDownloadingDialog != null) {
			mTokenDownloadingDialog.dismiss();
		}
	}
	
	@UiThread
	public void showToast(String text, int duration) {
		Toast.makeText(this, text, duration).show();
	}
	
	private boolean isNetworkAvailable() {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}

}
