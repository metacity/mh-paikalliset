package fi.metacity.klmobi;

import java.util.List;

public class Route {
	
	final List<RouteComponent> routeComponents;
	final float duration;
	final float distance;
	
	boolean isSelected;
	
	public Route(List<RouteComponent> routeComponents, float duration, float distance) {
		this.routeComponents = routeComponents;
		this.duration = duration;
		this.distance = distance;
	}
}
