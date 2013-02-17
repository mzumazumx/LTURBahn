import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	public static void main(String[] args) throws MalformedURLException,
			IOException {
		// "http://bahn.ltur.com/index/search/?lang=de_DE&searchin=DE-SB-VI&trip_mode=trip_simple&from_spar=$from&to_spar=$to&start_datum=$start_date&start_time=$start_time&end_datum=$end_date&end_time=$end_time&SEA_adults=1&trainclass_spar=2";

		String from = "Salzburg+Hbf";
		String to = "Hannover+Hbf";

		// http://bahn.ltur.com/?mnd=de&lang=de_DE&searchin=DE-SB-VI&trip_mode=trip_simple&from_spar=Salzburg+Hbf&to_spar=Hannover+Hbf&start_datum=06.02.2013&start_time=18%3A21&end_datum=07.02.2013&end_time=18%3A21&SEA_adults=1&SEA_kids1=0&SEA_kids2=0&SEA_adult1=&SEA_adult2=&SEA_adult3=&SEA_adult4=&SEA_adult5=&SEA_kid11=&SEA_kid12=&SEA_kid13=&SEA_kid14=&SEA_kid15=&trainclass_spar=2&x=54&y=7#

		String from_date = "06.02.2013";
		String from_time = "08%3A00";

		String to_date = "7.02.2013";
		String to_time = "14%3A00";

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
		File html = saveHTMLtoFile("save.html", "http://bahn.ltur.com/details",
				cookies);
		Document doc = Jsoup.parse(html, "utf-8");
		Elements prices = doc.select("label");
		for (Element price : prices) {
			String text = price.text();
			if (text.endsWith("Û")) {
				double num = Double.parseDouble(text.substring(0, 2) + "."
						+ text.substring(3, 5));
				Element tr = price.parent().parent().parent();
				Elements tds = tr.select("td");
				System.out.println(num);
				for (int i = 0; i < tds.size(); i++) {
					String tdText = tds.get(i).text();
					if (i == 1 || i == 2)
						System.out.println(tdText);
				}
				System.out.println("---");
				System.out.println("---");
			}
		}
	}

	private static List<String> getCookie(String urlString) {
		List<String> cookies = new ArrayList<String>();
		URL url;
		try {
			url = new URL(urlString);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
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
		return cookies;
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
