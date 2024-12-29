package com.dayone.service;

import com.dayone.entity.CompanyEntity;
import com.dayone.entity.DividendEntity;
import com.dayone.exception.AlreadyTickerException;
import com.dayone.exception.NoCompanyException;
import com.dayone.exception.NoTickerException;
import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service // 싱글톤으로 관리됨
@AllArgsConstructor
public class CompanyService {
	private final Trie trie;

	private final Scraper yahooFinanceScraper;

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	public Company save(String ticker) {
		boolean exists = this.companyRepository.existsByTicker(ticker);
		if (exists) {
			throw new AlreadyTickerException();
		}
		return this.storeCompanyAndDividend(ticker);
	}

	public Page<CompanyEntity> getAllCompany(Pageable pageable) {
		return this.companyRepository.findAll(pageable  );
	}

	private Company storeCompanyAndDividend(String ticker) {
		// ticker 를 기준으로 회사를 스크래핑
		Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
		if (ObjectUtils.isEmpty(company)) {
			throw new NoTickerException();
		}

		// 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
		ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

		// 스크래핑 결과
		CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
		List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
												.map(e -> new DividendEntity(companyEntity.getId(), e))
												.collect(Collectors.toList());
		this.dividendRepository.saveAll(dividendEntities);

		return company;
	}

	public List<String> getCompanyNamesByKeyword(String keyword) {
		Pageable limit = PageRequest.of(0, 10); // 한번에 10개씩 가져올 수 있도록 페이징
		Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);

		return companyEntities.stream()
								.map(e -> e.getName())
								.collect(Collectors.toList());
	}

	public void addAutoCompleteKeyword(String keyword) {
		this.trie.put(keyword, null);
	}

	public List<String> autoComplete(String keyword) {
		return (List<String>) this.trie.prefixMap(keyword).keySet()
				.stream().collect(Collectors.toList());
		// page 나 limit 으로 회사명 개수를 조절 할 수 있음
	}

	public void deleteAutoCompleteKeyword(String keyword) {
		this.trie.remove(keyword);
	}

	public String deleteCompany(String ticker) {
		var company = this.companyRepository.findByTicker(ticker)
				.orElseThrow(() -> new NoCompanyException());

		this.dividendRepository.deleteAllByCompanyId(company.getId());
		this.companyRepository.delete(company);

		this.deleteAutoCompleteKeyword(company.getName());
		return company.getName();
	}
}
