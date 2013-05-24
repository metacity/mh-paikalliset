package fi.metacity.klmobi;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView.OnItemClickListener;

public class AddressClickListener implements OnItemClickListener {
	private final AutoCompleteTextView mSourceView;

	public AddressClickListener(AutoCompleteTextView sourceView) {
		mSourceView = sourceView;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Address selectedAddress = GlobalApp.locations.get(position);
		if (mSourceView.getId() == R.id.fromAddressTextView)
			GlobalApp.startAddress = selectedAddress;
		else
			GlobalApp.endAddress = selectedAddress;
	}

}
