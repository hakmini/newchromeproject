package com.example.newchromeproject.Service;

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
@Service
public class UpbitRestService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<MarketDto> getMarkets() {
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

}



            /*
            // JSON 문자열로 변환
            String jsonData = response.body().string();

            // JSON → List<UpbitMarketDto>
            return mapper.readValue(jsonData, new TypeReference<List<MarketDto>>() {});*/