package fi.metacity.klmobi;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.LongClick;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends SherlockActivity implements OnNavigationListener, 
OnTimeSetListener, OnDateSetListener {

	private final Calendar mDateTime = GregorianCalendar.getInstance();
	
	@App
	MHApp mGlobals;

	@ViewById(R.id.startText)
	AutoCompleteTextView mStartText;

	@ViewById(R.id.endText)
	AutoCompleteTextView mEndText;

	@ViewById(R.id.timeText)
	TextView mTimeText;

	@ViewById(R.id.dateText)
	TextView mDateText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);

		ArrayAdapter<CharSequence> citiesAdapter = ArrayAdapter.createFromResource(
				actionBar.getThemedContext(), 
				R.array.matkahuoltoCities,
				R.layout.sherlock_spinner_item
				);
		citiesAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(citiesAdapter, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		return true;
	}

	@AfterViews
	public void initializeUi() {
		setDateTimeTexts(mDateTime);
		mStartText.requestFocus();
	}
	
	private void setDateTimeTexts(Calendar dateTime) {
		String time = String.format("%02d:%02d", dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE));
		String date = dateTime.get(Calendar.DAY_OF_MONTH)+ "." 
				+ (dateTime.get(Calendar.MONTH) + 1) + "."     // Months in Calendar class are 0-11
				+ dateTime.get(Calendar.YEAR);

		mTimeText.setText(time);
		mDateText.setText(date);
	}

	@LongClick(R.id.swapBtn)
	public void onSwapButtonLongClicked() {
		Toast.makeText(this, getString(R.string.swapStartAndEnd), Toast.LENGTH_LONG).show();
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


}
