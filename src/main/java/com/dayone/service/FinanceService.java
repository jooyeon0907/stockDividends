package com.dayone.service;

import com.dayone.entity.CompanyEntity;
import com.dayone.entity.DividendEntity;
import com.dayone.exception.NoCompanyException;
import com.dayone.model.constants.CacheKey;
import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	// 캐싱 전 고려사항
		// 요청이 자주 들어오는가?
		// 자주 변경되는 데이터 인가?
	// cache 의 데이터가 없을 경우, 아래 로직을 실행 후 리턴 값을 cache 에 추가
	// cache 의 데이터가 있을 경우, 아래 로직을 실행시키지 않고 cache 에 있는 데이터를 바로 리턴
	@Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
	public ScrapedResult getDividendByCompanyName(String companyName) {
		log.info("search company -> " + companyName);
		// 1. 회사명을 기준으로 회사 정보 조회
		CompanyEntity company = this.companyRepository.findByName(companyName)
									.orElseThrow(() -> new NoCompanyException());

		// 2. 조회된 회사 ID로 배당금 정보 조회
		List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

		// 3. 결과 조합 후 반환

		// Entity 를 model 로 바꾸는 방법
			// 1. for 문 사용하여 list 에 담기
			// 2. stream 사용

		// 1. for 문 사용하여 list 에 담기
//		List<Dividend> dividends = new ArrayList<>();
//		for (var entity : dividendEntities) {
//			dividends.add(Dividend.builder()
//							.date(entity.getDate())
//							.dividend(entity.getDividend())
//							.build());
//		}

		// 2. stream 사용
		List<Dividend> dividends = dividendEntities.stream()
													.map(e -> new Dividend(e.getDate(), e.getDividend()))
													.collect(Collectors.toList());

		return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
	}
}
