package test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class IntegrationTester {
	private static final int BUFFER_SIZE = 1024;

	private String theSession;

	public void login(String aUrl, String aBody) throws Exception {
		URL url = new URL(aUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		post(conn, aBody);
		theSession = getCookie(conn, "JSESSIONID");
	}

	protected int post(HttpURLConnection aConnection, String aBody) throws Exception {
		aConnection.setRequestProperty("Content-Type", "application/json");
		aConnection.setRequestProperty("Cookie", theSession);
		aConnection.setRequestMethod("POST");
		aConnection.setDoOutput(true);
		OutputStream os = aConnection.getOutputStream();
		Writer w = new OutputStreamWriter(os);
		w.write(aBody);
		w.close();
		os.close();
		return aConnection.getResponseCode();
	}

	protected String getResponse(HttpURLConnection aConnection) throws IOException {
		Reader in = new InputStreamReader(aConnection.getInputStream());
		char[] buf = new char[BUFFER_SIZE];
		StringBuilder b = new StringBuilder();
		int count = 0;
		while ((count = in.read(buf)) == BUFFER_SIZE) {
			b.append(buf);
		}
		b.append(buf, 0, count);
		return b.toString();
	}

	public String test(String aUrl, String aBody) throws Exception {
		URL url = new URL(aUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		int status = post(conn, aBody);
		System.out.println("Status = " + status);
		return getResponse(conn);
	}

	protected String getCookie(HttpURLConnection aConnection, String aName) {
		for (String cookie : aConnection.getHeaderFields().get("Set-Cookie")) {
			cookie = cookie.substring(0, cookie.indexOf(";"));
			String cookieName = cookie.substring(0, cookie.indexOf("="));
			if (cookieName.equals(aName)) {
				return cookie;
			}
		}
		return null;
	}
}
