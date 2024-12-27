package com.dayone.scraper;

import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.Month;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {
	private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d";
	private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

	private static final long START_TIME = 86400; // 60 * 60 * 24

	@Override
	public ScrapedResult scrap(Company company) {
		var scrapResult = new ScrapedResult();
		scrapResult.setCompany(company);

		try {
			long now = System.currentTimeMillis() / 1000;

			String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get();

            Elements parsingDivs = document.getElementsByAttributeValue("class", "table yf-j5d1ld noDl");
            Element tableEle = parsingDivs.get(0); // table 전체

            Element tbody = tableEle.children().get(1);

			List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }
                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

				if (month < 0){
					throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
				}

				dividends.add(
							Dividend.builder()
							.date(LocalDateTime.of(year, month, day, 0, 0))
							.dividend(dividend)
							.build()
				);
            }
			scrapResult.setDividends(dividends);

        } catch (Exception e) {
            e.printStackTrace();
        }

		return scrapResult;
	}

	@Override
	public Company  scrapCompanyByTicker(String ticker) {
		String url = String.format(SUMMARY_URL, ticker, ticker);

		try {
			Document document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .get();
            Elements titleEle = document.getElementsByAttributeValue("class", "yf-xxbei9");
//			String title = titleEle.text().split(" - ")[1].trim();
			String title = titleEle.text();
			System.out.println("title : "  +title);


			return Company.builder()
							.ticker(ticker)
							.name(title)
							.build();

		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
		}


}
