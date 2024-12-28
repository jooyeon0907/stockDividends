package com.dayone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // spring boot 에서 스케쥴러를 사용하기 위해 추가
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
