/*
 Copyright 2012 Jouni Latvatalo

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package fi.sandman.utils.coordinate;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A coordinate utility to convert between different coordinate systems.
 * </p>
 * 
 * <p>
 * This is just a port from Olli Lammi's python coverter, so kudos goes to him.
 * </p>
 * 
 * <p>
 * NOTE!: The coordinate functions are developed to work only with coordinates
 * that are in the area of Finland.
 * </p>
 * 
 * @author Jouni Latvatalo <jouni.latvatalo@gmail.com>
 * 
 */
public class CoordinateUtils {

	/**
	 * <p>
	 * {@code CoordinateUtils} instances should NOT be constructed in standard
	 * programming. Instead, the class should be used as
	 * {@code CoordinateUtils.convertFromKKJLaLoToWGS84LaLo(la,lo);}.
	 * </p>
	 * 
	 * <p>
	 * This constructor is public to permit tools that require a JavaBean
	 * instance to operate.
	 * </p>
	 */
	public CoordinateUtils() {
		super();
	}

	/**
	 * Converts KKJ xy coordinates to WGS84 format.
	 * 
	 * @param point
	 * @return
	 * @throws CoordinateConversionFailed
	 */
	public static CoordinatePoint convertKKJxyToWGS84lalo(CoordinatePoint point)
			throws CoordinateConversionFailed {
		CoordinatePoint pointKKJLaLo = convertKKJxyToKKJlalo(point);
		return convertKKJlaloToWGS84(pointKKJLaLo);
	}

	/**
	 * Converts KKJ latitude / longitude coordinates to WGS84 format.
	 * 
	 * @param point
	 * @return
	 */
	public static CoordinatePoint convertKKJlaloToWGS84(CoordinatePoint point) {
		double latitude = point.getLatitude();
		double longitude = point.getLongitude();

		double dLa = Math.toRadians(0.124867E+01 + -0.269982E+00 * latitude
				+ 0.191330E+00 * longitude + 0.356119E-02 * latitude * latitude
				+ -0.122312E-02 * latitude * longitude + -0.335514E-03
				* longitude * longitude) / 3600.0;
		double dLo = Math.toRadians(-0.286111E+02 + 0.114183E+01 * latitude
				+ -0.581428E+00 * longitude + -0.152421E-01 * latitude
				* latitude + 0.118177E-01 * latitude * longitude + 0.826646E-03
				* longitude * longitude) / 3600.0;

		CoordinatePoint pointWGS84 = new CoordinatePoint(Math.toDegrees(Math.toRadians(latitude)
				+ dLa), Math.toDegrees(Math.toRadians(longitude) + dLo));
		return pointWGS84;
	}

	/**
	 * Converts a KKJ x / y coordinate to KKJ latitude / longitude
	 * 
	 * @param point
	 * @return
	 * @throws CoordinateConversionFailed
	 */
	public static CoordinatePoint convertKKJxyToKKJlalo(CoordinatePoint point)
			throws CoordinateConversionFailed {

		int zoneNumber = 0;
		try {
			zoneNumber = getKKJzoneByEasting(point.getLongitude());
		} catch (UnableToDetermineZone e) {
			throw new CoordinateConversionFailed(e);
		}

		double minLa = Math.toRadians(59.0);
		double maxLa = Math.toRadians(70.5);
		double minLo = Math.toRadians(18.5);
		double maxLo = Math.toRadians(32.0);

		CoordinatePoint pointKKJLaLo = new CoordinatePoint(0, 0);
		for (int i = 1; i < 35; i++) {
			double deltaLa = maxLa - minLa;
			double deltaLo = maxLo - minLo;

			pointKKJLaLo.setLatitude(Math.toDegrees(minLa + 0.5 * deltaLa));
			pointKKJLaLo.setLongitude(Math.toDegrees(minLo + 0.5 * deltaLo));

			CoordinatePoint tmp = convertKKJlaloToKKJxy(pointKKJLaLo, zoneNumber);

			if (tmp.getLatitude() < point.getLatitude()) {
				minLa = minLa + 0.45 * deltaLa;
			} else {
				maxLa = minLa + 0.55 * deltaLa;
			}
			if (tmp.getLongitude() < point.getLongitude()) {
				minLo = minLo + 0.45 * deltaLo;
			} else {
				maxLo = minLo + 0.55 * deltaLo;
			}
		}
		return pointKKJLaLo;
	}

	/**
	 * Gets the KKJ zone number by easting
	 * 
	 * @param easting
	 * @return
	 * @throws UnableToDetermineZone
	 */
	public static int getKKJzoneByEasting(double easting)
			throws UnableToDetermineZone {
		int zoneNumber = (int) Math.floor(easting / 1000000.0);
		if (zoneNumber < 0 || zoneNumber > 5) {
			throw new UnableToDetermineZone(easting);
		}
		return zoneNumber;
	}

	public static int getKKJzoneByLongitude(double longitude) {
		int zoneNumber = 5;
		while (zoneNumber >= 0) {
			if (Math.abs(longitude - KKJ_ZONE_INFO.get(zoneNumber)[0]) <= 1.5) {
				break;
			}
			zoneNumber = zoneNumber - 1;
		}
		return zoneNumber;
	}

	public static CoordinatePoint convertKKJlaloToKKJxy(CoordinatePoint pointKKJLaLo, int zoneNumber) {
		double lo = Math.toRadians(pointKKJLaLo.getLongitude())
				- Math.toRadians(KKJ_ZONE_INFO.get(zoneNumber)[0]);

		// Hayford ellipsoids
		double aHayford = 6378388.0;
		double fHayford = 1 / 297.0;

		double b = (1.0 - fHayford) * aHayford;
		double bb = b * b;
		double c = (aHayford / b) * aHayford;
		double ee = (aHayford * aHayford - bb) / bb;
		double n = (aHayford - b) / (aHayford + b);
		double nn = n * n;

		double cosLa = Math.cos(Math.toRadians(pointKKJLaLo.getLatitude()));
		double nn2 = ee * cosLa * cosLa;
		double laF = Math.atan(Math.tan(Math.toRadians(pointKKJLaLo
				.getLatitude())) / Math.cos(lo * Math.sqrt(1 + nn2)));
		double cosLaF = Math.cos(laF);

		double t = (Math.tan(lo) * cosLaF)
				/ Math.sqrt(1 + ee * cosLaF * cosLaF);

		double a = aHayford / (1 + n);
		double a1 = a * (1 + nn / 4 + nn * nn / 64);
		double a2 = a * 1.5 * n * (1 - nn / 8);
		double a3 = a * 0.9375 * nn * (1 - nn / 4);
		double a4 = a * 35 / 48.0 * nn * n;

		double tLa = a1 * laF - a2 * Math.sin(2 * laF) + a3 * Math.sin(4 * laF)
				- a4 * Math.sin(6 * laF);
		double tLo = c * Math.log(t + Math.sqrt(1 + t * t)) + 500000.0
				+ zoneNumber * 1000000.0;
		
		return new CoordinatePoint(tLa, tLo);
	}

	public static CoordinatePoint convertWGS84lolaToKKJxy(CoordinatePoint pointWGS84)
			throws CoordinateConversionFailed {
		CoordinatePoint pointKKJLaLo = convertWGS84toKKJlalo(pointWGS84);
		int zoneNumber = getKKJzoneByLongitude(pointKKJLaLo.getLongitude());
		return convertKKJlaloToKKJxy(pointKKJLaLo, zoneNumber);
	}

	public static CoordinatePoint convertWGS84toKKJlalo(CoordinatePoint pointWGS84) {
		double latitude = pointWGS84.getLatitude();
		double longitude = pointWGS84.getLongitude();
		double dLa = StrictMath.toRadians(-1.24766 + 0.269941 * latitude
				+ -0.191342 * longitude + -0.00356086 * latitude * latitude
				+ 0.00122353 * latitude * longitude + 0.000335456 * longitude
				* longitude) / 3600;
		double dLo = StrictMath.toRadians(28.6008 + -1.14139 * latitude
				+ 0.581329 * longitude + 0.0152376 * latitude * latitude
				+ -0.0118166 * latitude * longitude + -0.000826201 * longitude
				* longitude) / 3600.0;

		CoordinatePoint pointKKJLaLo = new CoordinatePoint(Math.toDegrees(Math.toRadians(latitude)
				+ dLa), Math.toDegrees(Math.toRadians(longitude) + dLo));
		return pointKKJLaLo;
	}

	private static final Map<Integer, double[]> KKJ_ZONE_INFO = new HashMap<Integer, double[]>() {
		private static final long serialVersionUID = 1L;
		{
			put(0, new double[] { 18.0, 500000.0 });
			put(1, new double[] { 21.0, 1500000.0 });
			put(2, new double[] { 24.0, 2500000.0 });
			put(3, new double[] { 27.0, 3500000.0 });
			put(4, new double[] { 30.0, 4500000.0 });
			put(5, new double[] { 33.0, 5500000.0 });
		}
	};

}