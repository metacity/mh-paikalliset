package fi.metacity.klmobi;

import java.util.List;

public class Route {
	final List<RouteComponent> routeComponents;
	final double duration, distance;
	
	public Route(List<RouteComponent> routeComponents, double duration, double distance) {
		this.routeComponents = routeComponents;
		this.duration = duration;
		this.distance = distance;
	}
}
