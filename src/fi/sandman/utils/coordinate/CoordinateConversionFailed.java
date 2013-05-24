package fi.sandman.utils.coordinate;

/**
 * 
 * @author Jouni Latvatalo <jouni.latvatalo@gmail.com>
 * 
 */
public class CoordinateConversionFailed extends Exception {

	private static final long serialVersionUID = 1L;

	public CoordinateConversionFailed(Exception e) {
		super(e);
	}

}
