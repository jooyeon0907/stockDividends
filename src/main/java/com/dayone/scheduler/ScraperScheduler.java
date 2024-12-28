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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	private final YahooFinanceScraper yahooFinanceScraper;

	@Scheduled(fixedDelay = 1000)
	public void test1() throws InterruptedException {
		Thread.sleep(1000);
		System.out.println(Thread.currentThread().getName() + " -> 테스트 1 : " + LocalDateTime.now());
	}

	@Scheduled(fixedDelay = 1000)
	public void test2() {
		System.out.println(Thread.currentThread().getName() + " -> 테스트 2 : " + LocalDateTime.now());
	}
	// Thread pool 로 관리 전에는 test1() 과 test2() 가 같은 스레드에서 관리가 되어 test2는 test1 작업 10초가 지난 뒤에 수행이 되었는데,
	// Thread pool 적용 후에는 다른 스레드로 관리되어 각각 실행이 된 것을 확인


	// 일저 주기마다 수행
//	@Scheduled(cron = "${scheduler.scrap.yahoo}")
		// 서비스 제공 증에 변경될 수 있는 여지가 있는 스케쥴러 값 같은 경우는 config  설정 파일에 관리하는 것이 편리
			// 그렇지 않으면 스케쥴 값이 바뀔 때마다 빌드 및 배포 과정을 거쳐야하는 번거로움이 생김
	public void yahooFinanceScheduling() {
		log.info("scraping scheduler is started");
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
