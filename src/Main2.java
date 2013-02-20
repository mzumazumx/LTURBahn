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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main2 {

	private static List<Journey> journeys = new ArrayList<Journey>();
	
	private boolean journeyAlreadyFound(Journey journey) {
		for (Journey other : journeys) {
			if (other.equals(journey))
				return true;
		}
		return false;
	}

	public static void main(String[] args) throws MalformedURLException,
			IOException {
		// "http://bahn.ltur.com/index/search/?lang=de_DE&searchin=DE-SB-VI&trip_mode=trip_simple&from_spar=$from&to_spar=$to&start_datum=$start_date&start_time=$start_time&end_datum=$end_date&end_time=$end_time&SEA_adults=1&trainclass_spar=2";

		String from = "Ingolstadt+Hbf";
		String to = "Hannover+Hbf";

		// http://bahn.ltur.com/?mnd=de&lang=de_DE&searchin=DE-SB-VI&trip_mode=trip_simple&from_spar=Salzburg+Hbf&to_spar=Hannover+Hbf&start_datum=06.02.2013&start_time=18%3A21&end_datum=07.02.2013&end_time=18%3A21&SEA_adults=1&SEA_kids1=0&SEA_kids2=0&SEA_adult1=&SEA_adult2=&SEA_adult3=&SEA_adult4=&SEA_adult5=&SEA_kid11=&SEA_kid12=&SEA_kid13=&SEA_kid14=&SEA_kid15=&trainclass_spar=2&x=54&y=7#

		SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat sdfTime1 = new SimpleDateFormat("H");
		SimpleDateFormat sdfTime2 = new SimpleDateFormat("m");
		Calendar cal = new GregorianCalendar();
		String[] dates = new String[7];
		for (int i = 0; i < 7; i++) {
			cal.add(Calendar.DATE, 1);
			dates[i] = sdfDate.format(cal.getTime());
		}

		Date now = new Date();
		for (String from_date : dates) {
			// String from_date = sdfDate.format(now); //"22.02.2013";
//			String from_time = sdfTime1.format(now) + "%3A" + sdfTime2.format(now); // "14%3A00";
			String from_time = "05%3A00";

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
			for (String string : cookies) {
				System.out.println("cookie: " + string);
			}

			// getCookie("http://bahn.ltur.com/details", cookies);
			// for (String string : cookies) {
			// System.out.println("cookie: " + string);
			// }
			//
			// File html = saveHTMLtoFile("save.html",
			// "http://bahn.ltur.com/details/filter/set/1",
			// cookies);
			File html = saveHTMLtoFile("save.html",
					"http://bahn.ltur.com/details", cookies);

			Document doc = Jsoup.parse(html, "utf-8");
			Elements prices = doc.select("label");
			for (Element price : prices) {
				String text = price.text();
				if (text.endsWith("Û")) {
					Journey journey = new Journey();

					journey.price = Double.parseDouble(text.substring(0, 2)
							+ "." + text.substring(3, text.length()-1));
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

					journeys.add(journey);
				}
			}
		}
		Collections.sort(journeys);

		for (Journey journey : journeys) {
			System.out.println(journey);
		}
	}

	private static List<String> getCookie(String urlString, List<String> cookies) {
		List<String> cookiesInternal = new ArrayList<String>();
		URL url;
		try {
			url = new URL(urlString);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setRequestProperty("Connection", "Keep-Alive");
			for (String cookie : cookies) {
				urlConn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
			HttpURLConnection httpConn = (HttpURLConnection) urlConn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(false);
			httpConn.connect();
			cookies = httpConn.getHeaderFields().get("Set-Cookie");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cookiesInternal;
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
