package com.dayone.security;

import com.dayone.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

	private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour
	private static final String KEY_ROLES = "roles";

	private final MemberService memberService;

	@Value("${spring.jwt.secret}")
	private String secretKey;

	/**
	 * 토큰 생성(빌급)
	 * @param username
	 * @param roles
	 * @return
	 */
	public String generateToken(String username, List<String> roles) {
		Claims claims = Jwts.claims().setSubject(username); // 사용자 권한 정보
		claims.put(KEY_ROLES, roles);

		var now = new Date();
		var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now) // 토큰 생성 시간
				.setExpiration(expiredDate) // 토큰 만료 시간
				.signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 알고리즘과 비밀키
				.compact();
	}
	public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()); // 사용자 정보와 사용자 권한 정보를 포함한 토큰
    }

	public String getUsername(String token) {
		return this.paresClaims(token).getSubject();
	}

	public boolean validateToken(String token){
		if (!StringUtils.hasText(token)) return false; // 토큰이 유효하지 않다면 false 반환

		var claims = this.paresClaims(token);
		return !claims.getExpiration().before(new Date()); // 토큰의 만료시간을 현재 시간과 비교하여 만료 여부 반환
	}

	// 토큰이 유효한지 확인
	private Claims paresClaims(String token) {
		try{
			return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e){
        	return e.getClaims();
		}
	}


}
