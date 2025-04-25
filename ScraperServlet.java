import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//@WebServlet("/scrape")
class ScrapeServlet extends HttpServlet {

    public static class ScrapedData {
        String type;
        String content;

        public ScrapedData(String type, String content) {
            this.type = type;
            this.content = content;
        }

        public String getType() { return type; }
        public String getContent() { return content; }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws  IOException {

        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) visitCount = 1;
        else visitCount += 1;
        session.setAttribute("visitCount", visitCount);

        String url = request.getParameter("url");
        String[] options = request.getParameterValues("option");

        List<ScrapedData> scrapedDataList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();

            if (options != null) {
                for (String option : options) {
                    switch (option) {
                        case "title":
                            scrapedDataList.add(new ScrapedData("Title", doc.title()));
                            break;
                        case "headings":
                            for (int i = 1; i <= 6; i++) {
                                Elements hs = doc.select("h" + i);
                                for (Element h : hs) {
                                    scrapedDataList.add(new ScrapedData("Heading h" + i, h.text()));
                                }
                            }
                            break;
                        case "links":
                            for (Element link : doc.select("a[href]")) {
                                scrapedDataList.add(new ScrapedData("Link", link.attr("abs:href")));
                            }
                            break;
                    }
                }
            }

        } catch (IOException e) {
            scrapedDataList.add(new ScrapedData("Error", e.getMessage()));
        }


        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Scrape Results</title></head><body>");
        out.println("<h2>Scraping Results for: " + url + "</h2>");
        out.println("<p>You have visited this page " + visitCount + " times.</p>");
        out.println("<table border='1' id='resultsTable'><tr><th>Type</th><th>Content</th></tr>");

        for (ScrapedData data : scrapedDataList) {
            out.println("<tr><td>" + data.type + "</td><td>" + data.content + "</td></tr>");
        }

        out.println("</table><br>");
        out.println("<button onclick='downloadCSV()'>Download CSV</button>");
        out.println("<br><br><a href='index.html'>Back</a>");

        Gson gson = new Gson();
        String json = gson.toJson(scrapedDataList);
        out.println("<h3>JSON Output:</h3><pre>" + json + "</pre>");

        out.println("<script>" +
                "function downloadCSV() {" +
                "let csv = 'Type,Content\\n';" +
                "const rows = document.querySelectorAll('table tr');" +
                "rows.forEach(row => {" +
                "const cols = row.querySelectorAll('td, th');" +
                "csv += Array.from(cols).map(td => '\"' + td.innerText + '\"').join(',') + '\\n';" +
                "});" +
                "const blob = new Blob([csv], { type: 'text/csv' });" +
                "const a = document.createElement('a');" +
                "a.href = URL.createObjectURL(blob);" +
                "a.download = 'scraped_data.csv';" +
                "a.click();" +
                "}" +
                "</script>");

        out.println("</body></html>");
    }
}
