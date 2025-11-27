package com.example.newchromeproject.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FirstTickerDto {
    private String code;
    private String koreanName;
    private String englishName;
    private double tradePrice;
    private double changeRate;
    private String changeState;
    private double accTradePrice;
}