package fi.metacity.klmobi;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;

@EFragment
public class RouteDetailsFragment extends Fragment {

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
		if (mGlobals.getStartAddress() == null) {
			getActivity().finish();
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		
		ExpandableListView expandableList = new ExpandableListView(getActivity()); 
		expandableList.setLayoutParams(new LayoutParams(	
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		if (mRouteIndex >= 0 && mRouteIndex < mGlobals.getRoutes().size()) {
			List<RouteComponent> routeComponents = mGlobals.getRoutes().get(mRouteIndex).routeComponents;
			expandableList.setAdapter(new RouteDetailsAdapter(getActivity(), routeComponents));

			for (int i = expandableList.getExpandableListAdapter().getGroupCount()-1; i >= 0; --i) {
				expandableList.expandGroup(i);
			}
		}

		return expandableList;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Button showInMapButton = (Button) getActivity().findViewById(R.id.showInMapBtn);
		if (showInMapButton != null) {
			showInMapButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					RouteGMapActivity_.intent(getActivity()).mRouteIndex(mRouteIndex)
							.flags(Intent.FLAG_ACTIVITY_NO_HISTORY).start();
				}
			});
		}
	}

}
