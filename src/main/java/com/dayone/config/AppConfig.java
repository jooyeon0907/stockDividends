package com.dayone.config;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	public Trie<String, String> trie() { // 싱글톤으로 관리하기 위해 bean 으로 등록
		return new PatriciaTrie<>();
	}
}
