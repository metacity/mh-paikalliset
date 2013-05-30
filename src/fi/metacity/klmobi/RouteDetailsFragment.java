package fi.metacity.klmobi;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;

@EFragment
public class RouteDetailsFragment extends SherlockFragment {

	@App
	MHApp mGlobals;

	@FragmentArg(Constants.EXTRA_ROUTE_INDEX)
	int mRouteIndex;

	public static RouteDetailsFragment_ newInstance(int position) {
		RouteDetailsFragment_ detailsFragment = new RouteDetailsFragment_();
		Bundle args = new Bundle();
		args.putInt(Constants.EXTRA_ROUTE_INDEX, position);
		detailsFragment.setArguments(args);
		return detailsFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ExpandableListView expandableList = new ExpandableListView(getSherlockActivity()); 
		expandableList.setLayoutParams(new LayoutParams(	
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		expandableList.setGroupIndicator(null);

		if (mRouteIndex >= 0 && mRouteIndex < mGlobals.getRoutes().size()) {
			List<RouteComponent> routeComponents = mGlobals.getRoutes().get(mRouteIndex).routeComponents;
			expandableList.setAdapter(new RouteDetailsAdapter(getSherlockActivity(), routeComponents));
	
	
			for (int i = expandableList.getExpandableListAdapter().getGroupCount()-1; i >= 0; --i) {
				expandableList.expandGroup(i);
			}
		}

		return expandableList;
	}

}
