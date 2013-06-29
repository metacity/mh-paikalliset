package fi.metacity.klmobi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import fi.sandman.utils.coordinate.CoordinateConversionFailed;
import fi.sandman.utils.coordinate.CoordinatePoint;
import fi.sandman.utils.coordinate.CoordinateUtils;

import android.location.Location;
import android.net.Uri;

public class Utils {
	
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd;HHmm", Locale.ENGLISH);

	public static String httpPost(String endpoint, Map<String, String> params) throws IOException {
		URL url = new URL(endpoint);
		String response = "";
		
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(Uri.encode(param.getKey())).append('=')
				.append(URLEncoder.encode(param.getValue(), "UTF-8").replace("\\+", "%20"));
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();

		byte[] bytes = body.getBytes("UTF-8");
		HttpURLConnection conn = null;
		System.setProperty("http.keepAlive", "false");
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Close");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			
			conn.connect();
			
			// Post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.flush();
			out.close();
			
			// Handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("POST failed with error code " + status);
			}
			
			// Getting the InputStream has to happen before getting the status code
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new BufferedInputStream(conn.getInputStream()), "UTF-8"), 8192);
			
			
			
			// Read the response
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				responseBuilder.append(line);
				responseBuilder.append("\n");
			}
			response = responseBuilder.toString();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return response;
	}
	
	public static String httpGet(String endpoint) throws IOException {
		URL url = new URL(endpoint);
		String response = "";
		
		HttpURLConnection conn = null;
		System.setProperty("http.keepAlive", "false");
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setRequestProperty("Connection", "close");
			conn.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
			
			// Getting the InputStream has to happen before getting the status code
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new BufferedInputStream(conn.getInputStream()), "UTF-8"), 8192);
			
			// handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("GET failed with error code " + status);
			}
			
			// Read the response
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				responseBuilder.append(line);
				responseBuilder.append("\n");
			}
			response = responseBuilder.toString();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return response;
	}

	public static Address locationToCoordinateAddress(Location location) {
		CoordinatePoint wgs84CoordPoint = new CoordinatePoint(location.getLatitude(), location.getLongitude());
		try {
			CoordinatePoint kkjCoordPoint = CoordinateUtils.convertWGS84lolaToKKJxy(wgs84CoordPoint);
			JSONObject json = new JSONObject();
			json.put("x", kkjCoordPoint.longitude);
			json.put("y", kkjCoordPoint.latitude);
			json.put("name", JSONObject.NULL);
			json.put("city", JSONObject.NULL);
			return new Address(json);
		} catch (CoordinateConversionFailed ccfex) {
			// Ignore
		} catch (JSONException jsonex) {
			// Ignore
		}
		return null;
	}

	public static String getAndroidAnnotationsLicense() {
		return "<p>Copyright 2012 eBusiness Information<p>"
				+ "<p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not"
				+ "use this file except in compliance with the License. You may obtain a copy of"
				+ "the License at</p>"
				+ "<p>http://www.apache.org/licenses/LICENSE-2.0</p>"
				+ "<p>Unless required by applicable law or agreed to in writing, software"
				+ "distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT"
				+ "WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the"
				+ "License for the specific language governing permissions and limitations under"
				+ "the License.<p>"
				+ "<p>This project uses CodeModel (http://codemodel.java.net/), which is"
				+ "distributed under the GlassFish Dual License, which means CodeModel is"
				+ "subject to the terms of either the GNU General Public License Version 2 only"
				+ "(\"GPL\") or the Common Development and Distribution License(\"CDDL\")."
				+ "You may obtain a copy of the \"CDDL\" License at</p>"
				+ "<p>http://www.opensource.org/licenses/cddl1.php</p>"
				+ "<p>As per section 3.6 (\"Larger Works\") of the \"CDDL\" License, we may create a"
				+ "Larger Work by combining Covered Software with other code not governed by"
				+ "the terms of this License and distribute the Larger Work as a single product."
				+ "We are therefore allowed to distribute CodeModel without Modification as"
				+ "part of AndroidAnnotations.</p>";
	}
	
	public static String getJsoupLicense() {
		return "<p>The MIT License<br>" + 
				"Copyright © 2009 - 2013 Jonathan Hedley (jonathan@hedley.net)</p>" + 
				"<p>Permission is hereby granted, free of charge, to any person obtaining " +
				"a copy of this software and associated documentation files (the \"Software\"), " +
				"to deal in the Software without restriction, including without limitation " +
				"the rights to use, copy, modify, merge, publish, distribute, sublicense, " +
				"and/or sell copies of the Software, and to permit persons to whom the Software " +
				"is furnished to do so, subject to the following conditions:</p>" + 
				"<p>The above copyright notice and this permission notice shall be included in " +
				"all copies or substantial portions of the Software.</p>" +
				"<p>THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, " +
				"EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, " +
				"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS " +
				"OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER " +
				"IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION " +
				"WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.</p>";
	}
	
	public static String getCoordinateUtilsLicense() {
		return "<p>Copyright 2012 Jouni Latvatalo</p>"
				+ "<p>Licensed under the Apache License, Version 2.0 (the \"License\"); "
				+ "you may not use this file except in compliance with the License. "
				+ "You may obtain a copy of the License at</p>"
				+ "<p>http://www.apache.org/licenses/LICENSE-2.0</p>"
				+ "<p>Unless required by applicable law or agreed to in writing, "
				+ "software distributed under the License is distributed on an \"AS IS\""
				+ "BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "
				+ "See the License for the specific language governing permissions and limitations "
				+ "under the License.</p>";
	}
	
	public static String getUrlImageViewHelperLicense() {
		return "<p>Copyright 2012 Koushik Dutta</p><p>Licensed under the Apache License, " +
				"Version 2.0 (the \"License\"); you may not use this file except in compliance " +
				"with the License. You may obtain a copy of the License at</p>" +
				"<p>http://www.apache.org/licenses/LICENSE-2.0</p>" +
				"<p>Unless required by applicable law or agreed to in writing, " +
				"software distributed under the License is distributed on an \"AS IS\" " +
				"BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. " +
				"See the License for the specific language governing permissions and limitations " +
				"under the License.";
	}
}
