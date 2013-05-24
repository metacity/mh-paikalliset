package fi.metacity.klmobi;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	private TextView mTimeTextView;
	
	public static TimePickerFragment getInstance(int targetTextViewResourceId) {
		TimePickerFragment timePicker = new TimePickerFragment();
		
		Bundle args = new Bundle();
		args.putInt("textViewResource", targetTextViewResourceId);
		timePicker.setArguments(args);
		return timePicker;
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		int textViewResource = getArguments().getInt("textViewResource", 0);
		mTimeTextView = (TextView) getActivity().findViewById(textViewResource);
		
        // Use the time from editText4 as the default
		String[] rawTimeComponents = mTimeTextView.getText().toString().split(":");
        int hour = Integer.parseInt(rawTimeComponents[0]);
        int minute = Integer.parseInt(rawTimeComponents[1]);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mTimeTextView.setText(KlmobiUtils.niceTimeFormat(hourOfDay, minute));
	}

}
