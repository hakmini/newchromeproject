package com.example.newchromeproject.Service;

import com.example.newchromeproject.dto.FirstTickerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickerSnapshotServiceTest {

    private static final int COIN_COUNT = 289; //업비트 원화 마켓 종목 수

    private FirstTickerDto ticker(int i, double price) {
        return FirstTickerDto.builder()
                .code("KRW-COIN" + i)
                .koreanName("코인" + i)
                .englishName("Coin" + i)
                .tradePrice(price)
                .changeState("EVEN")
                .build();
    }

    private List<FirstTickerDto> initialTickers() {
        List<FirstTickerDto> tickers = new ArrayList<>();
        for (int i = 0; i < COIN_COUNT; i++) tickers.add(ticker(i, 0));
        return tickers;
    }

    @Test
    @DisplayName("시세가 갱신되면 스냅샷 조회 결과에 최신 값이 반영된다")
    void updateReplacesLatestTicker() {
        TickerSnapshotService snapshot = new TickerSnapshotService();
        snapshot.init(initialTickers());

        snapshot.update(ticker(5, 1234));

        List<FirstTickerDto> all = snapshot.getAll();
        assertEquals(COIN_COUNT, all.size());
        assertEquals(1234, all.get(5).getTradePrice());
    }

    @Test
    @DisplayName("갱신이 반복되어도 처음 받은 종목 순서가 유지된다")
    void keepsInitialOrder() {
        TickerSnapshotService snapshot = new TickerSnapshotService();
        snapshot.init(initialTickers());

        for (int i = COIN_COUNT - 1; i >= 0; i--) snapshot.update(ticker(i, i));

        List<FirstTickerDto> all = snapshot.getAll();
        for (int i = 0; i < COIN_COUNT; i++) {
            assertEquals("KRW-COIN" + i, all.get(i).getCode());
        }
    }

    @Test
    @DisplayName("여러 스레드가 동시에 쓰고 읽어도 예외 없이 전체 종목이 조회된다")
    void concurrentReadAndWrite() throws InterruptedException {
        TickerSnapshotService snapshot = new TickerSnapshotService();
        snapshot.init(initialTickers());

        int writers = 8, readers = 8, loops = 20_000;
        ExecutorService pool = Executors.newFixedThreadPool(writers + readers);
        CountDownLatch start = new CountDownLatch(1); //모든 스레드를 동시에 출발시키기 위한 장치
        CountDownLatch done = new CountDownLatch(writers + readers);
        Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

        for (int w = 0; w < writers; w++) { //웹소켓 수신 스레드 역할(쓰기)
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < loops; i++) {
                        int coin = ThreadLocalRandom.current().nextInt(COIN_COUNT);
                        snapshot.update(ticker(coin, i + 1));
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        for (int r = 0; r < readers; r++) { //요청 처리 스레드 역할(읽기)
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < loops / 10; i++) {
                        List<FirstTickerDto> all = snapshot.getAll();
                        if (all.size() != COIN_COUNT) {
                            throw new IllegalStateException("조회된 종목 수: " + all.size());
                        }
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "제한 시간 안에 끝나지 않음");
        pool.shutdownNow();

        assertTrue(errors.isEmpty(), "동시 접근 중 예외 발생: " + errors);
        assertEquals(COIN_COUNT, snapshot.getAll().size());
    }
}
