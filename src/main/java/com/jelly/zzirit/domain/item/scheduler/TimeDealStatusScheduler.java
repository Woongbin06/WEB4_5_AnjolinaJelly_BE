// package com.jelly.zzirit.domain.item.scheduler;
//
// import java.time.LocalDateTime;
//
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
//
// import com.jelly.zzirit.domain.item.service.TimeDealSchedulerService;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// @ConditionalOnProperty(name = "custom.scheduler.activated", havingValue = "true") // 조건을 만족할 때만 빈으로 등록
// public class TimeDealStatusScheduler {
//
// 	private final TimeDealSchedulerService timeDealSchedulerService;
//
// 	@Scheduled(fixedRate = 60_000) // 1분마다 실행
// 	public void updateTimeDealStatuses() {
// 		LocalDateTime now = LocalDateTime.now();
//
// 		boolean started = timeDealSchedulerService.startScheduledDeals(now);
// 		boolean ended = timeDealSchedulerService.endOngoingDeals(now);
//
// 		log.info("스케쥴러 동작: 시작 {}. 종료 {}.",
// 			started ? "수행" : "미수행",
// 			ended ? "수행" : "미수행");
// 	}
// }
