package com.dayone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

		int n  = Runtime.getRuntime().availableProcessors(); // 코어 개수 가져오기
		threadPool.setPoolSize(n);
		threadPool.initialize();

		taskRegistrar.setTaskScheduler(threadPool);

		/*
		- Thread Pool 의 적정 사이즈?
			- CPU 처리가 많은 경우
				- CPU 코어 개수 + 1개
			- I/O 작업이 많은 경우
				- CPU 코어 개수 * 2개
		 */
	}
}
