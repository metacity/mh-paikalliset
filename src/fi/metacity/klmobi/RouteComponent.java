package fi.metacity.klmobi;

import java.util.Date;
import java.util.List;

public class RouteComponent {
	
	final String code;
	final String startName;
	final String endName;
	
	final float duration;
	final float distance;
	final Date startDateTime;
	final Date endDateTime;
	final List<WayPoint> wayPoints;

	public RouteComponent(
			String code, 
			String startName, 
			String endName,
			Date startDateTime, 
			Date endDateTime, 
			float duration, 
			float distance, 
			List<WayPoint> wayPoints) {
		
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
