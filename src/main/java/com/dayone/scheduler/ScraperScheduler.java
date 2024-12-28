package com.dayone.scheduler;

import com.dayone.entity.CompanyEntity;
import com.dayone.entity.DividendEntity;
import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.scraper.YahooFinanceScraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	private final YahooFinanceScraper yahooFinanceScraper;

	// 일저 주기마다 수행
	@Scheduled(cron = "0 0 0 * * *")
	public void yahooFinanceScheduling() {
		// 저장된 회사 목록을 조회
		List<CompanyEntity> companies = this.companyRepository.findAll();

		// 회사마다 배당금 정보를 새로 스크래핑
		for (var company : companies) {
			log.info("scraping scheduler is started -> " + company.getName());
			ScrapedResult scrapedResult =
									this.yahooFinanceScraper.scrap(Company.builder()
																	.name(company.getName())
																	.ticker(company.getTicker())
																	.build());

			// 스크래핑한 배당금 정보 중 DB에 없는 값은 저장
			scrapedResult.getDividends().stream()
					// dividend model 을 dividend entity 로 매핑
					.map(e -> new DividendEntity(company.getId(), e))
					// element 를 하나씩 dividend repository 에 삽입
					.forEach(e -> {
							boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
							if (!exists) {
								this.dividendRepository.save(e);
							}
					});

			// 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
			try {
				Thread.sleep(3000); // 3 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

}
