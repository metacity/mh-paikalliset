package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EFragment(R.layout.history)
public class HistoryDialog extends DialogFragment {

	@Pref
	Preferences_ mPreferences;

	@ViewById(R.id.historyList)
	ListView mHistoryList;

	private ArrayAdapter<Address> mAdapter;
	private OnHistorySelectedListener mSelectedListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.history);
		setRetainInstance(true);
		return dialog;
	}

	@AfterViews
	public void initHistory() {
		try {
			parseAndSetHistory();
		} catch (JSONException jsonex) {
			Log.e("init", jsonex.toString());
		}
	}

	public void setSelectedListener(OnHistorySelectedListener listener) {
		mSelectedListener = listener;
	}

	private void parseAndSetHistory() throws JSONException {
		// Init the list
		List<Address> history = new ArrayList<Address>();
		JSONArray historyJson = new JSONArray(mPreferences.addressHistory().get());
		for (int i = 0; i < historyJson.length(); ++i) {
			Address address = new Address(historyJson.getJSONObject(i));
			history.add(address);
		}
		mAdapter = new HistoryAdapter(history);
		mHistoryList.setAdapter(mAdapter);
		mHistoryList.setSelector(R.drawable.selector_orange);
	}

	private class HistoryAdapter extends ArrayAdapter<Address> {
		private final List<Address> mHistory;

		public HistoryAdapter(List<Address> history) {
			super(getActivity(), R.layout.history_row, history);
			mHistory = history;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView;
			HistoryHolder holder;

			if (v == null) {
				holder = new HistoryHolder();
				v = View.inflate(getActivity(), R.layout.history_row, null);

				holder.historyNameText = (TextView) v.findViewById(R.id.historyName);
				holder.historyAddressText = (TextView) v.findViewById(R.id.historyAddress);

				v.setTag(holder);
			}
			else {
				holder = (HistoryHolder) v.getTag();
			}

			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mSelectedListener.onHistorySelected(mAdapter.getItem(position));
					dismiss();
				}
			});

			Address address = mHistory.get(position);
			String userGivenName = address.userGivenName();
			String fullAddress = address.fullAddress();
			if (userGivenName.length() == 0 || userGivenName.trim().equals(fullAddress.trim())) {
				holder.historyNameText.setText(fullAddress);
				holder.historyAddressText.setText("");
			} else {
				holder.historyNameText.setText(userGivenName);
				holder.historyAddressText.setText(fullAddress);
			}
			
			return v;
		}
	}

	private static class HistoryHolder {
		TextView historyNameText;
		TextView historyAddressText;
	}

	public static interface OnHistorySelectedListener {
		void onHistorySelected(Address selectedAddress);
	}


}
