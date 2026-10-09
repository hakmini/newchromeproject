package com.example.newchromeproject;

import com.example.newchromeproject.Service.TickerSnapshotService;
import com.example.newchromeproject.Service.UpbitRestService;
import com.example.newchromeproject.dto.FirstTickerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FirstTickerController {

    private final UpbitRestService restService;
    private final TickerSnapshotService snapshotService;

    @GetMapping("/first-tickers")
    public List<FirstTickerDto> getFirstTickers() {
        if (snapshotService.isEmpty()) { //스냅샷이 아직 없을 때만(서버 시작 시 초기화 실패 등) 업비트 API 직접 호출
            return restService.getMergeTickersAndMarkets();
        }
        return snapshotService.getAll(); //평소에는 메모리의 스냅샷으로 바로 응답(외부 API 호출 0회)
    }
}
