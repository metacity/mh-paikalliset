package fi.metacity.klmobi;

import java.util.Date;
import java.util.List;

public class RouteComponent {
	final String code, startName, endName;
	final double duration, distance;
	final Date startDateTime, endDateTime;
	final List<WayPoint> wayPoints;

	public RouteComponent(String code, String startName, String endName,
			Date startDateTime, Date endDateTime, double duration, double distance, List<WayPoint> wayPoints) {
		this.code = code;
		this.startName = startName;
		this.endName = endName;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.duration = duration;
		this.distance = distance;
		this.wayPoints = wayPoints;
	}
}
