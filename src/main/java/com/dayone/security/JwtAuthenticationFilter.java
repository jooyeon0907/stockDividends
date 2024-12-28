package com.dayone.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String TOKEN_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";

	private final TokenProvider tokenProvider;

	// 요청이 들어올 때마다 필터가 컨트롤러에 실행되기 전에 먼저 실행되면서 요청의 헤더에 토큰이 있는지 확인하고,
	// 토큰이 유효하다면 인증정보를 context 에 담음
	// 유효하지 않다면 바로 실행됨
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String token = this.resolveTokenFromRequest(request);
		System.out.println("token : " +token);

		if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
			Authentication auth = this.tokenProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}

		filterChain.doFilter(request, response);
	}

	private String resolveTokenFromRequest(HttpServletRequest request) {
		String token = request.getHeader(TOKEN_HEADER);

		if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)){
			return token.substring(TOKEN_PREFIX.length());
		}

		return null;
	}
}
