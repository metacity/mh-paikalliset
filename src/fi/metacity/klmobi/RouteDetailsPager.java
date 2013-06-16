package fi.metacity.klmobi;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

public class RouteDetailsPager extends ViewPager {
	
	public RouteDetailsPager(Context context) {
		super(context);
	}

	public RouteDetailsPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof SurfaceView || v instanceof TextureView) {
			return true;
		}
        return super.canScroll(v, checkV, dx, x, y);
    }
}
