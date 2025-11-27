package com.example.newchromeproject.Service;

import com.example.newchromeproject.dto.MarketDto;
//import com.example.newchromeproject.Service.UpbitRestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor //의존성 주입 - 생성자 자동 주입
public class UpbitWebSocketService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper socket_mapper = new ObjectMapper();
    private final SimpMessagingTemplate messagingTemplate;
    private final UpbitRestService restService;
    private  Map<String, MarketDto> marketMap; //단 내부에서 초기화 되는 값은 일반 필드로 설정하기



    @PostConstruct // bean의 의존성 주입이 끝난 직후 자동 실행되는 초기화 로직(서버 실행 시 딱 1번만 실행되면 됨)
    public void readyToWebSocket() {

        //UpbitRestService로부터 market 데이터 받아오기
        try {
            List<MarketDto> markets = restService.getMarkets().stream()
                    .filter(m -> m.getMarket().startsWith("KRW-")) // m -> m2  m2에 맞는 조건만 객체 m에 추가하겠음을 의미.
                    .toList();

            //List -> Map 변환, key 값은 market 값으로
            this.marketMap = markets.stream() //marketMap은 전역변수이므로 this 꼭 사용
                    .collect(Collectors.toMap(m -> m.getMarket(), m -> m)); //MarketDto :: getMarket - 맵의 키값을 market로 사용하겠다.
                                                                              // m -> m - 맵의 value값은 dto로 사용하겠다.

        } catch (Exception e) {
            System.err.println("마켓 정보 가져오기 실패: " + e.getMessage());
        }

        startToWebSocket();
    }




        private void startToWebSocket() {
            Request request = new Request.Builder()
                    .url("wss://api.upbit.com/websocket/v1")
                    .build();


        //WebSocket 실시간 데이터 받아오기
            client.newWebSocket(request, new WebSocketListener() { //try-catch문을 써볼까 했는데 WebSocket은 비동기 방식이라 즉시 잡히는 예외가 없기에 필요없음
                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    try {
                    List<String> codes = new ArrayList<>(marketMap.keySet()); //키만 가져오기 KRW-~~~

                    String subscribeJson = socket_mapper.writeValueAsString(List.of( //아래 두 Map.of로 만들어진 Map 객체를 배열로 묶기
                            Map.of("ticket", "spring-server"),           // {"ticket" : "spring-server"}
                            Map.of("type", "ticker", "codes", codes) // {
                            ));                                                 //  "type" : "ticker",
                                                                                //  "codes" : ["KRW-BTC", "KRW-ETH", ...]
                                                                                // }  ----> java 객체를 json으로 만들어 웹소켓 서버에 보낼 준비 완료

                    webSocket.send(subscribeJson); //업비트 서버에 구독 요청
                    System.out.println("업비트 구독 시작 / 코인 종목 수 : "+ codes.size());
                    }

                    catch (Exception e) {
                    e.printStackTrace();
                    }

            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes){ // WebSocket 서버에서는 이진형식으로 데이터를 보내기 때문에 ByteString으로 받는다.
                try {                                                                       // WebSocket은 웹소켓 서버와 연결하기 위한 객체임.
                    String ticker_json = bytes.utf8(); // 웹소켓의 이진데이터 변환
                    JsonNode node = socket_mapper.readTree(ticker_json);

                    String code = node.get("code").asText();
                    double tradePrice = node.get("trade_price").asDouble();
                    double prevClosing = node.get("prev_closing_price").asDouble();
                    double changePrice = node.get("change_price").asDouble();
                    double changeRate = node.get("change_rate").asDouble() * 100; // 퍼센트 변환
                    double accTradePrice = node.get("acc_trade_price_24h").asDouble();
                    String changeState = node.get("change").asText(); // RISE / FALL / EVEN

                    MarketDto final_match = marketMap.get(code); // 중요 - rest,websocket로 받아온 데이터 매칭 코드
                    if (final_match != null) {

                        Map<String, Object> message = Map.of( // 클라이언트에게 보내는 실시간 데이터(1회성, 계속 새로운 데이터를 보내줘야하기 때문에 1회성으로 설정)
                                "code", code,
                                "koreanName", final_match.getKorean_name(),
                                "englishName", final_match.getEnglish_name(),
                                "tradePrice", tradePrice,
                                "changePrice", changePrice,
                                "changeRate", changeRate,
                                "changeState", changeState,
                                "accTradePrice", accTradePrice
                        );

                        messagingTemplate.convertAndSend("/topic/upbit", message);

                        System.out.printf("[%s] %s | 현재가: %.0f | 전일대비: %+.0f원 (%.2f%%, %s) | 거래대금(24h): %.0f%n", // 로깅용 코드
                                code,
                                final_match.getKorean_name(),
                                tradePrice,
                                changePrice,
                                changeRate,
                                changeState,
                                accTradePrice
                        );

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) { // 예외처리
                // 연결 실패 시
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                // 서버가 연결 닫을 때
            }
        });
    }
}