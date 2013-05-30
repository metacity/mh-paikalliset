package fi.metacity.klmobi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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
			bodyBuilder.append(param.getKey()).append('=')
			.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();

		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		System.setProperty("http.keepAlive", "false");
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "close");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			
			// Post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			
			// Getting the InputStream has to happen before getting the status code
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new BufferedInputStream(conn.getInputStream()), "UTF-8"), 8192);
			
			// Handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("POST failed with error code " + status);
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
	
	public static String httpGet(String endpoint) throws IOException {
		URL url = new URL(endpoint);
		String response = "";
		
		HttpURLConnection conn = null;
		System.setProperty("http.keepAlive", "false");
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Connection", "close");
			conn.setRequestProperty("Content-Type", "text/html");
			
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

}
