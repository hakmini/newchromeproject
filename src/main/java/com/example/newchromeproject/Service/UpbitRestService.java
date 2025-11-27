package com.example.newchromeproject.Service;

import com.example.newchromeproject.dto.FirstTickerDto;
import com.example.newchromeproject.dto.MarketDto;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;  // okhttp 구조 request(받아) -> response (받을게)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class UpbitRestService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<MarketDto> getMarkets() { //Upbit api는 데이터를 배열 형태로 보내기 때문에 List를 사용해야함.
        Request request = new Request.Builder()
                .url("https://api.upbit.com/v1/market/all")
                .get()
                .addHeader("accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) { // 동기처리(execute) + try-with-resource 문법으로 예외처리
            if (!response.isSuccessful()) {
                throw new IOException("서버 응답 오류" + response);
            }

            ResponseBody body = response.body(); // response가 null일 경우 예외처리
            if (body == null) {
                throw new IOException("페어 목록 null 발생");
            }

            String jsonData_pair = body.string();

            return mapper.readValue(
                    jsonData_pair, new TypeReference<List<MarketDto>>() {}
            );


        } catch (IOException e) {
            throw new RuntimeException("페어 목록 조회 실패", e);
        }
    }



    public List<MarketDto> getKrwFilterMarkets() {
        return getMarkets()
                .stream()
                .filter(m -> m.getMarket().startsWith("KRW-"))
                .toList();
    }

    public List<JsonNode> getFirstTickers(List<String> markets) { //markets에는 KRW-BTC,KRW-ETH..들어감

        String marketParam = String.join(",", markets);

        Request request = new Request.Builder()
                .url("https://api.upbit.com/v1/ticker?markets=" + marketParam)
                .get()
                .addHeader("accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Ticker API 오류: " + response);
            }

            ResponseBody body = response.body();
            if (body == null) throw new IOException("Ticker null 발생");

            String json = body.string();
            return mapper.readValue(json, new TypeReference<List<JsonNode>>() {});

        } catch (IOException e) {
            throw new RuntimeException("초기 Ticker 조회 실패", e);
        }
    }

    public List<FirstTickerDto> getMergeTickersAndMarkets() {

        List<MarketDto> krwFilterMarkets = getKrwFilterMarkets(); //MarketDto 불러오기

        List<String> krwCodes = krwFilterMarkets.stream() //불러온 MarketDto에서 KRW만 뺴오기 -> [KRW-BTC,KRW-ETH...]
                .map(MarketDto::getMarket)
                .toList();
        
        List<JsonNode> firstTickers = getFirstTickers(krwCodes); //ticker?markets=KRW-BTC,KRW-ETH,.. 요청

        Map<String, MarketDto> marketMap = krwFilterMarkets.stream()
                .collect(Collectors.toMap(MarketDto::getMarket, m -> m)); // "KRW-BTC" : MarketDto(한글명, 영문명) 추출


        return firstTickers.stream()
                .map(t -> {
                    String code = t.get("market").asText(); //code는 KRW-BTC,KRW-ETH,..
                    MarketDto m = marketMap.get(code); // 이름 매칭

                    return FirstTickerDto.builder()
                            .code(code)
                            .koreanName(m.getKorean_name())
                            .englishName(m.getEnglish_name())
                            .tradePrice(t.get("trade_price").asDouble())
                            .changeState(t.get("change").asText())
                            .changeRate(t.get("signed_change_rate").asDouble() * 100)
                            .accTradePrice(t.get("acc_trade_price_24h").asDouble())
                            .build();
                })
                .toList(); //DTO 리스트 반환
    }


}


