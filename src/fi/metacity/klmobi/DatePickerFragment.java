package fi.metacity.klmobi;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	private TextView mDateTextView;
	
	public static DatePickerFragment getInstance(int targetTextViewResourceId) {
		DatePickerFragment datePicker = new DatePickerFragment();
		
		Bundle args = new Bundle();
		args.putInt("textViewResource", targetTextViewResourceId);
		datePicker.setArguments(args);
		return datePicker;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		int textViewResource = getArguments().getInt("textViewResource", 0);
		mDateTextView = (TextView) getActivity().findViewById(textViewResource);
		
		String[] rawDateComponents = mDateTextView.getText().toString().split("/");
        int day = Integer.parseInt(rawDateComponents[0]);
        int month = Integer.parseInt(rawDateComponents[1]) - 1;
        int year = Integer.parseInt(rawDateComponents[2]);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

	@Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        mDateTextView.setText(KlmobiUtils.niceDateFormat(day, month, year));
    }

}
