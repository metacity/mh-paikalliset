package fi.metacity.klmobi;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@EFragment(R.layout.favourites)
public class FavouritesDialog extends DialogFragment {

	@Pref
	Preferences_ mPreferences;

	@FragmentArg(Constants.EXTRA_SAVABLE_FAVOURITE)
	String mSaveableFavourite;
	
	@ViewById(R.id.addFavouriteLayout)
	RelativeLayout mAddFavouriteLayout;

	@ViewById(R.id.addFavouriteInfoText)
	TextView mAddFavouritesInfoText;
	
	@ViewById(R.id.addFavouriteBtn)
	Button mAddFavouritesBtn;
	
	@ViewById(R.id.favouriteUserGivenNameField)
	EditText mUserGivenNameField;

	@ViewById(R.id.favouritesList)
	ListView mFavouritesList;

	private ArrayAdapter<Address> mAdapter;
	private OnFavouriteSelectedListener mSelectedListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.favouritesDialogTitle);
		setRetainInstance(true);
		return dialog;
	}

	@AfterViews
	public void initFavourites() {
		try {
			parseAndSetFavourites();
		} catch (JSONException jsonex) {
			Log.e("init", jsonex.toString());
		}
	}

	public void setSelectedListener(OnFavouriteSelectedListener listener) {
		mSelectedListener = listener;
	}

	private void parseAndSetFavourites() throws JSONException {
		// Init the list
		List<Address> favourites = new ArrayList<Address>();
		JSONArray favouritesJson = new JSONArray(mPreferences.savedFavourites().get());
		for (int i = 0; i < favouritesJson.length(); ++i) {
			Address favourite = new Address(favouritesJson.getJSONObject(i));
			favourites.add(favourite);
		}
		mAdapter = new FavouriteAdapter(favourites);
		mFavouritesList.setAdapter(mAdapter);
		mFavouritesList.setSelector(R.drawable.selector_orange);

		// Set the savable favourites
		if (favourites.size() == 0 && mSaveableFavourite.length() == 0) {
			mAddFavouritesInfoText.setText(getString(R.string.noFavourites));
			mAddFavouritesBtn.setVisibility(View.GONE);
			mUserGivenNameField.setVisibility(View.GONE);
		} else if (mSaveableFavourite.length() == 0) {
			mAddFavouriteLayout.setVisibility(View.GONE);
		} else {
			String addFavText = getString(R.string.addToFavourites) + "\n"
					+ new Address(new JSONObject(mSaveableFavourite)).toString();
			mAddFavouritesInfoText.setText(addFavText);
		}
	}

	@Click(R.id.addFavouriteBtn)
	public void addFavourite() {
		try {
			Address addressToSave = new Address(new JSONObject(mSaveableFavourite));
			String userGivenName = mUserGivenNameField.getText().toString();
			if (userGivenName.length() > 0) {
				addressToSave.json.put("user_given_name", userGivenName);
			}
			JSONArray favourites = new JSONArray(mPreferences.savedFavourites().get());
			favourites.put(addressToSave.json);
			mPreferences.savedFavourites().put(favourites.toString());
			Toast.makeText(getActivity(), "\"" + addressToSave.toString() + "\" " 
					+ getString(R.string.addedToFavourites), Toast.LENGTH_LONG).show();
			dismiss();
		} catch (JSONException jsonex) {
			Log.e("FAVS", jsonex.toString());
		}
	}

	private void removeFavourites(int position) {
		try {
			JSONArray updatedFavourites = new JSONArray();
			JSONArray savedFavourites = new JSONArray(mPreferences.savedFavourites().get());
			Address addressToDelete = new Address(savedFavourites.getJSONObject(position));

			for (int i = 0; i < savedFavourites.length(); ++i) {
				if (i != position) updatedFavourites.put(savedFavourites.getJSONObject(i));
			}
			mPreferences.savedFavourites().put(updatedFavourites.toString());
			Toast.makeText(getActivity(), "\"" + addressToDelete.toString() + "\" " 
					+ getString(R.string.removedFromFavourites), Toast.LENGTH_LONG).show();
			dismiss();
		} catch (JSONException jsonex) {
			Log.e("FAVS", jsonex.toString());
		}
	}
	

	private class FavouriteAdapter extends ArrayAdapter<Address> {
		private final List<Address> mFavourites;

		public FavouriteAdapter(List<Address> favourites) {
			super(getActivity(), R.layout.favourites_row, favourites);
			mFavourites = favourites;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView;
			FavouriteHolder holder;

			if (v == null) {
				holder = new FavouriteHolder();
				v = View.inflate(getActivity(), R.layout.favourites_row, null);

				holder.favouriteNameText = (TextView) v.findViewById(R.id.favouriteName);
				holder.favouriteAddressText = (TextView) v.findViewById(R.id.favouriteAddress);
				holder.deleteFavouriteBtn = (ImageButton) v.findViewById(R.id.favouritesRemoveBtn);

				v.setTag(holder);
			}
			else {
				holder = (FavouriteHolder) v.getTag();
			}

			holder.deleteFavouriteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					removeFavourites(position);
				}
			});

			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mSelectedListener.onFavouriteSelected(mAdapter.getItem(position));
					Log.i("click", "listened called..");
					dismiss();
				}
			});

			Address address = mFavourites.get(position);
			String userGivenName = address.userGivenName();
			String fullAddress = address.fullAddress();
			if (userGivenName.length() == 0 || userGivenName.trim().equals(fullAddress.trim())) {
				holder.favouriteNameText.setText(fullAddress);
				holder.favouriteAddressText.setText("");
			} else {
				holder.favouriteNameText.setText(userGivenName);
				holder.favouriteAddressText.setText(fullAddress);
			}
			
			return v;
		}
	}

	private static class FavouriteHolder {
		TextView favouriteNameText;
		TextView favouriteAddressText;
		ImageButton deleteFavouriteBtn;
	}

	public static interface OnFavouriteSelectedListener {
		void onFavouriteSelected(Address selectedAddress);
	}


}
