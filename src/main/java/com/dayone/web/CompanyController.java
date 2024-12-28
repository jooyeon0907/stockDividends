package com.dayone.web;

import com.dayone.entity.CompanyEntity;
import com.dayone.model.Company;
import com.dayone.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {
	private final CompanyService companyService;

	@GetMapping("/autocomplete")
	public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
		var result = this.companyService.getCompanyNamesByKeyword(keyword);
		return ResponseEntity.ok(result);
	}

	@GetMapping
	@PreAuthorize("hasRole('READ')") // 권한 정보 걸기 -> read 권한이 있는 유저만 해당 api 호출 가능
	public ResponseEntity<?> searchCompany(final Pageable pageable) {
		Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
		return ResponseEntity.ok(companies);
	}

	/**
	 * 회사 및 배당금 정보 추가
	 * @param request
	 * @return
	 */
	@PostMapping
	@PreAuthorize("hasRole('WRITE')") // 권한 정보 걸기 -> write 권한이 있는 유저만 해당 api 호출 가능
	public ResponseEntity<?> addCompany(@RequestBody Company request) {
		String ticker = request.getTicker().trim();
		if (ObjectUtils.isEmpty(ticker)) {
			throw new RuntimeException("ticker is empty");
		}

		Company company = this.companyService.save(ticker);
		this.companyService.addAutoCompleteKeyword(company.getName());
		return ResponseEntity.ok(company);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteCompany(@RequestParam String ticker) {
		return null;
	}

}
