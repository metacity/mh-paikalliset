//package fi.metacity.klmobi;
//
//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.app.DialogFragment;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//
//import com.googlecode.androidannotations.annotations.EFragment;
//
//@EFragment
//public class FavouritesDialogFragment extends DialogFragment {
//	private static Dialog sInstance;
//	
//	@Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//		//final SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE);
//		//final Set<String> favouriteLocationsSet = settings.getStringSet("favouriteLocations", new LinkedHashSet<String>(0));
//
//		final int targetViewRef = getArguments().getInt("targetView", 0);
//		
//		final List<Address> savedAddresses = new ArrayList<Address>();
//		savedAddresses.add(new Address(new JSONObject())); // DUMMY OBJECT FOR "ADD TO FAVS" item
//		for (String stringJson : favouriteLocationsSet) {
//			try { 
//				savedAddresses.add(new Address(new JSONObject(stringJson)));
//			} catch (JSONException jsonex) {
//				System.err.println(jsonex);
//			}
//		}
//		
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setTitle(getResources().getString(R.string.favouritesDialogTitle))
//		       .setNegativeButton(getResources().getString(R.string.cancel), 
//		    		   new DialogInterface.OnClickListener() {
//			            public void onClick(DialogInterface dialog, int id) {
//			                // User cancelled the dialog
//			            }
//		       })
//		       .setAdapter(
//				new FavouriteAdapter(getActivity(), savedAddresses, settings, targetViewRef, null),
//				new DialogInterface.OnClickListener() {
//		            public void onClick(DialogInterface dialog, int id) {
//		                dialog.dismiss();
//		            }
//				});
//		       
//		
//		instance = builder.create();
//		//dialog.getListView();
//		return instance;
//	}
//	
//	private static class onAddressClickedListener
//	
//	
//}
