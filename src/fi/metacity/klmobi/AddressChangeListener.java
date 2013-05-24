package fi.metacity.klmobi;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

public class AddressChangeListener implements TextWatcher {
	private static final Map<String, Object> params = new HashMap<String, Object>();
	static {
		params.put("language", "fi");
		params.put("maxresults", 30);
	}
	private static String url;

	private final AutoCompleteTextView mSourceView;
	private final Context mContext;
	private final AQuery aq;

	public AddressChangeListener(Context context, AutoCompleteTextView sourceView, int cityIndex) {
		mContext = context;
		mSourceView = sourceView;
		aq = new AQuery(context);
		refreshUrl(cityIndex);
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().trim().length() == 0)
			return;

		// Always make new ArrayAdapter object, won't update loading text correctly otherwise!
		mSourceView.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, 
				new String[] { mContext.getString(R.string.loadingText) } ));

		params.put("token", GlobalApp.token);
		params.put("key", s.toString());

		aq.ajaxCancel().ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {

				if (json != null) {
					//successful ajax call, show status code and json content
					synchronized (AddressChangeListener.this) {
						GlobalApp.locations.clear();
						try {
							if (json.getInt("status") == 0) {
								JSONArray locationsArr = json.getJSONObject("data").getJSONArray("locations");
								for (int i = 0, len = locationsArr.length(); i < len; ++i) {
									GlobalApp.locations.add(new Address(locationsArr.getJSONObject(i)));
									//notifyFromAndTo();
								}
								// Always make new ArrayAdapter object, won't update suggestions correctly otherwise!
								ArrayAdapter<Address> locationsAdapter = new ArrayAdapter<Address>(mContext, 
										android.R.layout.simple_list_item_1, GlobalApp.locations);
								mSourceView.setAdapter(locationsAdapter);
								locationsAdapter.notifyDataSetChanged();
							} 
							else {
								//Toast.makeText(context, context.getString(R.string.addressNotFound), Toast.LENGTH_LONG).show();
								mSourceView.dismissDropDown();
							}
						} catch (JSONException jsonex) {
							Toast.makeText(mContext, jsonex.toString(), Toast.LENGTH_LONG).show();
						}
					}
				} else {
					//ajax error, show error code
					Toast.makeText(mContext, "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
					mSourceView.dismissDropDown();
				}
				status.invalidate();
			}
		});
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// Ignore
	}
	
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		AQuery clearButton = aq.id(
				(mSourceView.getId() == R.id.fromAddressTextView) ? R.id.fromClearBtn : R.id.toClearBtn
		);
		if (mSourceView.getText().toString().length() == 0)
			clearButton.invisible();
		else
			clearButton.visible();
	}

	public static void refreshUrl(int cityIndex) {
		url = "http://" + Constants.citySubdomains[cityIndex] + ".matkahuolto.info/geocode.php";
	}

	public static void notifyAdapters(ArrayAdapter<?>... adapters) {
		for (ArrayAdapter<?> adapter : adapters) {
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	}
}
