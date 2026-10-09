package com.example.newchromeproject.Service;

import com.example.newchromeproject.dto.FirstTickerDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class TickerSnapshotService { //최신 시세를 서버 메모리에 보관(스냅샷) -> 초기 시세 요청마다 업비트 API를 호출하지 않기 위함

    //웹소켓 수신 스레드(쓰기)와 요청 처리 스레드(읽기)가 동시에 접근하므로 HashMap 대신 ConcurrentHashMap 사용
    private final Map<String, FirstTickerDto> snapshot = new ConcurrentHashMap<>();
    private volatile List<String> codeOrder = List.of(); //업비트가 내려준 종목 순서 유지용(Map은 순서를 보장하지 않음)

    public void init(List<FirstTickerDto> tickers) { //서버 시작 시 1번 - REST로 받아온 초기 시세로 스냅샷 채우기
        tickers.forEach(t -> snapshot.put(t.getCode(), t));
        this.codeOrder = tickers.stream()
                .map(FirstTickerDto::getCode)
                .toList();
    }

    public void update(FirstTickerDto ticker) { //웹소켓으로 시세가 들어올 때마다 해당 종목만 덮어쓰기
        snapshot.put(ticker.getCode(), ticker);
    }

    public List<FirstTickerDto> getAll() { //스냅샷 전체 조회(외부 API 호출 없음)
        return codeOrder.stream()
                .map(snapshot::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean isEmpty() {
        return codeOrder.isEmpty();
    }
}
