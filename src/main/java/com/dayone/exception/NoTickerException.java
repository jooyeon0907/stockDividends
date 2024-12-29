package com.dayone.exception;

import org.springframework.http.HttpStatus;

public class NoTickerException extends AbstractException {
	@Override
	public int getStatusCode() {
		return HttpStatus.BAD_REQUEST.value();
	}

	@Override
	public String getMessage() {
		return "존재하지 않는 회사의 ticker 입니다.";
	}
}
