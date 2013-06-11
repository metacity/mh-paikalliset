package fi.metacity.klmobi;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

@SharedPref(value=Scope.UNIQUE)
public interface Preferences {
	
	@DefaultString("http://hameenlinna.matkahuolto.info/")
	String baseUrl();

	int selectedCityIndex();
	
	@DefaultString("[]")
	String savedFavourites();
}
