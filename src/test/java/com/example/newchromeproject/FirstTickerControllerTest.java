package com.example.newchromeproject;

import com.example.newchromeproject.Service.TickerSnapshotService;
import com.example.newchromeproject.Service.UpbitRestService;
import com.example.newchromeproject.dto.FirstTickerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FirstTickerControllerTest {

    private final FirstTickerDto btc = FirstTickerDto.builder()
            .code("KRW-BTC")
            .koreanName("비트코인")
            .englishName("Bitcoin")
            .tradePrice(100)
            .build();

    @Test
    @DisplayName("스냅샷이 있으면 업비트 API를 호출하지 않고 스냅샷으로 응답한다")
    void returnsSnapshotWithoutCallingUpbit() {
        UpbitRestService restService = mock(UpbitRestService.class); //실제 업비트 호출 대신 호출 여부만 기록하는 가짜 객체
        TickerSnapshotService snapshot = new TickerSnapshotService();
        snapshot.init(List.of(btc));
        FirstTickerController controller = new FirstTickerController(restService, snapshot);

        for (int i = 0; i < 100; i++) { //요청 100번
            List<FirstTickerDto> result = controller.getFirstTickers();
            assertEquals("KRW-BTC", result.get(0).getCode());
        }

        verifyNoInteractions(restService); //외부 API 호출 0회
    }

    @Test
    @DisplayName("스냅샷이 비어 있으면 업비트 API를 직접 호출해 응답한다")
    void fallsBackToUpbitWhenSnapshotIsEmpty() {
        UpbitRestService restService = mock(UpbitRestService.class);
        when(restService.getMergeTickersAndMarkets()).thenReturn(List.of(btc));
        FirstTickerController controller = new FirstTickerController(restService, new TickerSnapshotService());

        List<FirstTickerDto> result = controller.getFirstTickers();

        assertEquals(1, result.size());
        verify(restService, times(1)).getMergeTickersAndMarkets();
    }
}
