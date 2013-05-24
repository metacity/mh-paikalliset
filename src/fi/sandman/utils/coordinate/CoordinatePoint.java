package fi.sandman.utils.coordinate;

import java.io.Serializable;

/**
 * Describes a single point in the given Coordinate Reference System.
 * 
 * @author Jouni Latvatalo <jouni.latvatalo@gmail.com>
 * 
 */
public class CoordinatePoint implements Serializable {

	private static final long serialVersionUID = 4489750270195706147L;

	public double latitude;
	public double longitude;
	public double altitude;
	public CoordinateReferenceSystem coordinateReferenceSystem;

	/**
	 * Not meant to be instantiated without latitude and longitude
	 */
	@SuppressWarnings("unused")
	private CoordinatePoint() {
	}

	/**
	 * 
	 * @param latitude
	 *            (aka y, northing)
	 * @param longitude
	 *            (aka x, easting)
	 */
	public CoordinatePoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public CoordinatePoint(double latitude, double longitude, double altitude,
			CoordinateReferenceSystem coordinateReferenceSystem) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.coordinateReferenceSystem = coordinateReferenceSystem;
	}

	/**
	 * 
	 * @param latitude
	 *            (aka y, northing)
	 * @param longitude
	 *            (aka x, easting)
	 * @param coordinateReferenceSystem
	 */
	public CoordinatePoint(double latitude, double longitude,
			CoordinateReferenceSystem coordinateReferenceSystem) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.coordinateReferenceSystem = coordinateReferenceSystem;
	}

	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return coordinateReferenceSystem;
	}

	public void setCoordinateReferenceSystem(
			CoordinateReferenceSystem coordinateReferenceSystem) {
		this.coordinateReferenceSystem = coordinateReferenceSystem;
	}

	/**
	 * Get the latitude, aka
	 * 
	 * <ul>
	 * <li>First horizontal coordinate</li>
	 * <li>Y</li>
	 * <li>Northing</li>
	 * </ul>
	 * 
	 * @return
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get the latitude, aka
	 * 
	 * <ul>
	 * <li>First horizontal coordinate</li>
	 * <li>Y</li>
	 * <li>Northing</li>
	 * </ul>
	 * 
	 * @return
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Get the longitude, aka
	 * 
	 * <ul>
	 * <li>Second horizontal coordinate</li>
	 * <li>X</li>
	 * <li>Easting</li>
	 * </ul>
	 * 
	 * @return
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Set the longitude, aka
	 * 
	 * <ul>
	 * <li>Second horizontal coordinate</li>
	 * <li>X</li>
	 * <li>Easting</li>
	 * </ul>
	 * 
	 * @return
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * 
	 * @return
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * 
	 * @param altitude
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	
	
	public int getLongitudeMicrodegree() {
		return (int) (getLongitude() * 1E6);
	}

	public int getLatitudeMicrodegree() {
		return (int) (getLatitude() * 1E6);
	}
}
