package fi.metacity.klmobi;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface Preferences {
	
	@DefaultString("http://hameenlinna.matkahuolto.info/")
	String baseUrl();

	int selectedCityIndex();
	
	@DefaultString("[]")
	String savedFavourites();
	
	
	@DefaultString("[]")
	String addressHistory();
	
	@DefaultString("")
	String token();
	
	long tokenLastSyncTime();
}
