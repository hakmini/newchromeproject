package com.example.newchromeproject;

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

    @GetMapping("/first-tickers")
    public List<FirstTickerDto> getFirstTickers() {
        return restService.getMergeTickersAndMarkets();
    }
}
