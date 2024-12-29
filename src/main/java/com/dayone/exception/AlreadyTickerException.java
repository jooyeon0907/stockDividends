package com.dayone.exception;

import org.springframework.http.HttpStatus;

public class AlreadyTickerException extends AbstractException {
	@Override
	public int getStatusCode() {
		return HttpStatus.BAD_REQUEST.value();
	}

	@Override
	public String getMessage() {
		return "이미 존재하는 회사의 ticker 입니다.";
	}
}
