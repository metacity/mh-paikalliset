package fi.metacity.klmobi;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FavouriteAdapter extends ArrayAdapter<Address> {
	private final List<Address> mFavourites;
	private final Context mContext;
	private final SharedPreferences mPreferences;
	private final int mTargetViewRef;
	private final DialogInterface.OnClickListener mCloseCallback;

	public FavouriteAdapter(Context context, List<Address> favourites, 
			SharedPreferences preferences, int targetViewRef, DialogInterface.OnClickListener closeCallback) {
		super(context, R.layout.favourites_row, favourites);
		mContext = context;
		mFavourites = favourites;
		mPreferences = preferences;
		mTargetViewRef = targetViewRef;
		mCloseCallback = closeCallback;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		FavouriteHolder holder;

		if (v == null) {
			holder = new FavouriteHolder();
			v = View.inflate(mContext, R.layout.favourites_row, null);

			holder.favouriteView = (TextView) v.findViewById(R.id.textView1);
			holder.separatorView = (TextView) v.findViewById(R.id.textView2);
			holder.deleteFavouriteBtn = (ImageButton) v.findViewById(R.id.fromClearBtn);

			v.setTag(holder);
		}
		else {
			holder = (FavouriteHolder) v.getTag();
		}

		final SharedPreferences.Editor editor = mPreferences.edit();
		if (position == 0) { // "ADD COURSE TO FAVS" item
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONObject addressToBeSaved;
					if (mTargetViewRef == R.id.fromAddressTextView && GlobalApp.startAddress != null)
						addressToBeSaved = GlobalApp.startAddress.jsonAddress;
					else if (mTargetViewRef == R.id.toAddressTextView && GlobalApp.endAddress != null)
						addressToBeSaved = GlobalApp.endAddress.jsonAddress;
					else {
						Toast.makeText(mContext, mContext.getResources().getString(R.string.noSaveableLocationgFound), 
								Toast.LENGTH_LONG).show();
						return;
					}

					Set<String> updatedFavouriteLocationsSet = new LinkedHashSet<String>();
					for (int i = 1, len = mFavourites.size(); i < len; ++i) { // start from 1, 0 is the dummy object
						updatedFavouriteLocationsSet.add(mFavourites.get(i).jsonAddress.toString());
					}
					updatedFavouriteLocationsSet.add(addressToBeSaved.toString());
					editor.putStringSet("favouriteLocations", updatedFavouriteLocationsSet);
					editor.commit();

					Toast.makeText(mContext, "\"" + new Address(addressToBeSaved).toString() + "\" " 
							+ mContext.getResources().getString(R.string.addedToFavourites), Toast.LENGTH_LONG).show();
					MainActivity.sFavouritesDialog.dismiss();
				}
			});

			holder.favouriteView.setText(mContext.getResources().getString(R.string.addToFavourites));
			holder.deleteFavouriteBtn.setImageResource(R.drawable.ic_action_add);
			holder.deleteFavouriteBtn.setEnabled(false);
			holder.deleteFavouriteBtn.setClickable(false);
			holder.separatorView.setVisibility(View.INVISIBLE);
		} else { // a saved favourite item
			holder.deleteFavouriteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Set<String> updatedFavouriteLocationsSet = new LinkedHashSet<String>();
					for (int i = 1, len = mFavourites.size(); i < len; ++i) // start from 1, 0 is the dummy object
						if (i != position)
							updatedFavouriteLocationsSet.add(mFavourites.get(i).jsonAddress.toString());

					editor.putStringSet("favouriteLocations", updatedFavouriteLocationsSet);
					editor.commit();

					Toast.makeText(mContext, "\"" + new Address(mFavourites.get(position).jsonAddress).toString() + "\" " 
							+ mContext.getResources().getString(R.string.removedFromFavourites), Toast.LENGTH_LONG).show();
					MainActivity.sFavouritesDialog.dismiss();
				}
			});

			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Address selectedFavourite = mFavourites.get(position);
					if (mTargetViewRef == R.id.fromAddressTextView) {
						GlobalApp.startAddress = selectedFavourite;
					} else {
						GlobalApp.endAddress = selectedFavourite;

					}
					AutoCompleteTextView aotv = (AutoCompleteTextView) ((Activity) mContext).findViewById(mTargetViewRef);
					aotv.setText(selectedFavourite.toString());
					aotv.dismissDropDown();
					MainActivity.sFavouritesDialog.dismiss();
				}
			});

			Address address = mFavourites.get(position); // "ADD TO FAVOURITES" will be first in Dialog list, hence -1
			holder.favouriteView.setText(address.toString());
		}

		return v;
	}

	private static class FavouriteHolder {
		TextView favouriteView, separatorView;
		ImageButton deleteFavouriteBtn;
	}
}