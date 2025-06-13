// package com.jelly.zzirit.domain.item.queue;
//
// import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
// import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
// import jakarta.annotation.PostConstruct;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Component;
//
// import java.util.List;
//
// import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.ONGOING;
// import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.SCHEDULED;
//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class TimeDealQueueInitializer {
//
//     private final TimeDealRepository timeDealRepository;
//     private final TimeDealTaskProducer timeDealTaskProducer;
//
//     @PostConstruct
//     public void init() { // 서버 재시작 시 유실된 작업 등록
//         List<TimeDeal> activeTimeDeals = timeDealRepository.findInitTimeDeals(List.of(SCHEDULED, ONGOING));
//
//         for (TimeDeal timeDeal : activeTimeDeals) {
//             try {
//                 timeDealTaskProducer.enqueueTimeDealTasks(timeDeal);
//             } catch (InterruptedException e) { // 스레드 종료 요청 수신
//                 Thread.currentThread().interrupt(); // 인터럽트 플래그 복원
//                 log.error("딜레이 큐 초기화 중 인터럽트 발생: 타임 딜 아이디={}", timeDeal.getId(), e);
//             } catch (Exception e) {
//                 log.error("딜레이 큐 초기화 중 예외 발생: 타임 딜 아이디={}", timeDeal.getId(), e);
//             }
//         }
//     }
//
// }
