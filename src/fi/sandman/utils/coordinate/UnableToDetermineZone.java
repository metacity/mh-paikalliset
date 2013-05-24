package fi.sandman.utils.coordinate;

/**
 * Thrown if the zone for a given easting (KKJ y) can't be determined
 * 
 * @author Jouni Latvatalo <jouni.latvatalo@gmail.com>
 * 
 */
public class UnableToDetermineZone extends Exception {

	private static final long serialVersionUID = 1L;

	public UnableToDetermineZone(double easting) {
		super("Unable determine zone for easting " + easting);
	}

}
