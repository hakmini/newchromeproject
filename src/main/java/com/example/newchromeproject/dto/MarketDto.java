package com.example.newchromeproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class MarketDto {
    private String market; //final에 대해서
    private String korean_name;
    private String english_name;
}
//확장여부
//isActive 거래 중인 마켓인지 여부 (API엔 없지만 나중에 캐싱 시 유용)
//marketType KRW / BTC / USDT 마켓 구분용
//createdAt 캐싱 시점 표시 (DB 저장용)