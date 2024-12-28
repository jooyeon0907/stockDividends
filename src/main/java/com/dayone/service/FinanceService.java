package com.dayone.service;

import com.dayone.entity.CompanyEntity;
import com.dayone.entity.DividendEntity;
import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	public ScrapedResult getDividendByCompanyName(String companyName) {

		// 1. 회사명을 기준으로 회사 정보 조회
		CompanyEntity company = this.companyRepository.findByName(companyName)
									.orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));

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
				.map(e -> Dividend.builder()
						.date(e.getDate())
						.dividend(e.getDividend())
						.build())
				.collect(Collectors.toList());

		return new ScrapedResult(Company.builder()
									.ticker(company.getTicker())
									.name(company.getName())
									.build(),
								dividends);
	}
}
