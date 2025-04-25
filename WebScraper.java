import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class WebScraper {
    public static void main(String[] args) {


        try {
            Document doc = Jsoup.connect(url).get();


            System.out.println("Title: " + doc.title());


            for (int i = 1; i <= 6; i++) {
                Elements headings = doc.select("h" + i);
                for (Element heading : headings) {
                    System.out.println("Heading h" + i + ": " + heading.text());
                }
            }

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                System.out.println("Link: " + link.attr("abs:href") + " | Text: " + link.text());
            }

        } catch (IOException e) {
            System.out.println("Error fetching the URL: " + e.getMessage());
        }
    }
}
