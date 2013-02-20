import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	private static List<Journey> journeys = new ArrayList<Journey>();

	private static boolean journeyAlreadyFound(Journey journey) {
		for (Journey other : journeys) {
			if (other.myEquals(journey))
				return true;
		}
		return false;
	}

	public static void main(String[] args) throws MalformedURLException,
			IOException {
		String from = "Ingolstadt+Hbf";
		String to = "Hamburg+Hbf";

		SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat sdfTime1 = new SimpleDateFormat("H");
		SimpleDateFormat sdfTime2 = new SimpleDateFormat("m");
		Calendar cal = new GregorianCalendar();
		int interval = 6;
		for (int j = 0; j < 7 * 24 / interval; j++) {
			cal.add(Calendar.HOUR, interval);

			String from_date = sdfDate.format(cal.getTime());
			String from_time = sdfTime1.format(cal.getTime()) + "%3A"
					+ sdfTime2.format(cal.getTime()); // "14%3A00";

			String to_date = from_date;
			String to_time = from_time;

			StringBuilder queryURLBuilder = new StringBuilder(
					"http://bahn.ltur.com/index/search/?lang=de_DE&searchin=DE-SB-VI&trip_mode=trip_simple&from_spar=");
			queryURLBuilder.append(from);
			queryURLBuilder.append("&to_spar=");
			queryURLBuilder.append(to);
			queryURLBuilder.append("&start_datum=");
			queryURLBuilder.append(from_date);
			queryURLBuilder.append("&start_time=");
			queryURLBuilder.append(from_time);
			queryURLBuilder.append("&end_datum=");
			queryURLBuilder.append(to_date);
			queryURLBuilder.append("&end_time=");
			queryURLBuilder.append(to_time);
			queryURLBuilder
					.append("&SEA_adults=1&SEA_adult1=BC50-2&trainclass_spar=2");

			List<String> cookies = getCookie(queryURLBuilder.toString());
			File html = saveHTMLtoFile("save.html",
					"http://bahn.ltur.com/details", cookies);

			Document doc = Jsoup.parse(html, "utf-8");
			Elements prices = doc.select("label");

			boolean foundSomething = false;
			boolean foundSomethingNew = false;

			for (Element price : prices) {
				String text = price.text();
				if (text.endsWith("Û")) {
					Journey journey = new Journey();

					journey.price = Double.parseDouble(text.substring(0, 2)
							+ "." + text.substring(3, text.length() - 1));
					Element tr = price.parent().parent().parent();
					Elements tds = tr.select("td");
					for (int i = 0; i < tds.size(); i++) {
						String[] tdText = tds.get(i).text().split(" ");
						if (i == 1) {
							journey.from_date = tdText[0] + " " + tdText[1];
							journey.to_date = tdText[2] + " " + tdText[3];
						}
						if (i == 2) {
							journey.from_time = tdText[0] + " " + tdText[1];
							journey.to_time = tdText[2] + " " + tdText[3];
						}
					}
					if (!journeyAlreadyFound(journey)) {
						System.out.println(journey);
						foundSomethingNew = true;
						journeys.add(journey);
					} else {
						foundSomething = true;
					}
				}
			}
			if (!foundSomething && !foundSomethingNew) {
				System.out.println("no results in request #" + j);
			} else if (!foundSomethingNew) {
				System.out.println("no new results in request #" + j);
			}
		}

		Collections.sort(journeys);

		for (Journey journey : journeys) {
			System.out.println(journey);
		}
	}

	private static List<String> getCookie(String urlString) {
		List<String> cookiesInternal = new ArrayList<String>();
		URL url;
		try {
			url = new URL(urlString);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) urlConn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(false);
			httpConn.connect();
			cookiesInternal = httpConn.getHeaderFields().get("Set-Cookie");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cookiesInternal;
	}

	public static File saveHTMLtoFile(String filename, String urlString,
			List<String> cookies) throws MalformedURLException, IOException {
		InputStream in = null;
		FileOutputStream fout = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setRequestProperty("Connection", "Keep-Alive");
			for (String cookie : cookies) {
				urlConn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
			urlConn.connect();
			in = urlConn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(in);
			fout = new FileOutputStream(filename);

			int read = 0;
			int bufSize = 512;
			byte[] buffer = new byte[bufSize];
			while (true) {
				read = bis.read(buffer);
				if (read == -1) {
					break;
				}
				fout.write(buffer, 0, read);
			}
		} finally {
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
		return new File(filename);
	}

}
