package fi.metacity.klmobi;

import android.content.Context;
import android.os.Build;
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
		boolean icsOrNewer = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
		if (icsOrNewer && (v instanceof SurfaceView || v instanceof TextureView)) {
			return true;
		} else if (v instanceof SurfaceView) {
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}
