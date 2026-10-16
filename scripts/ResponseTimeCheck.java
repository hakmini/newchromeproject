import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * 초기 시세 API(/api/first-tickers) 응답 시간 측정용 프로그램
 *
 * 사용법 (서버를 먼저 실행해 둔 상태에서)
 *   java scripts/ResponseTimeCheck.java
 *   java scripts/ResponseTimeCheck.java [주소] [요청 횟수] [요청 사이 간격(ms)]
 *   예) java scripts/ResponseTimeCheck.java http://localhost:8080/api/first-tickers 30 300
 *
 * 워밍업 요청을 먼저 보낸 뒤(JVM이 덜 데워진 초반 요청은 느리기 때문) 지정한 횟수만큼 호출해 평균/중앙값을 출력한다.
 * 실패한 요청(200이 아닌 응답)은 시간 계산에서 빼고 횟수만 센다.
 * 출력은 콘솔 인코딩에 따라 한글이 깨질 수 있어 영어로 한다.
 */
public class ResponseTimeCheck {

    private static final int WARM_UP = 10; //측정에서 제외할 워밍업 요청 수

    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "http://localhost:8080/api/first-tickers";
        int count = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        long intervalMs = args.length > 2 ? Long.parseLong(args[2]) : 0;

        for (int i = 0; i < WARM_UP; i++) {
            request(url);
            Thread.sleep(intervalMs);
        }

        List<Double> times = new ArrayList<>(); //성공한 요청의 응답 시간(ms)
        int failed = 0;
        for (int i = 0; i < count; i++) {
            double time = request(url);
            if (time < 0) failed++;
            else times.add(time);
            Thread.sleep(intervalMs);
        }

        System.out.println("url: " + url);
        System.out.printf("requests %d | success %d | failed %d%n", count, times.size(), failed);
        if (times.isEmpty()) return;

        Collections.sort(times);
        int n = times.size();
        double avg = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double median = n % 2 == 1 ? times.get(n / 2) : (times.get(n / 2 - 1) + times.get(n / 2)) / 2;

        System.out.printf("avg %.1fms | median %.1fms | min %.1fms | max %.1fms%n",
                avg, median, times.get(0), times.get(n - 1));
    }

    private static double request(String url) { //요청 1번에 걸린 시간(ms) 반환, 실패하면 -1
        long start = System.nanoTime();

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Connection", "close"); //매번 새로 연결(팝업을 새로 여는 사용자와 같은 조건)
            if (conn.getResponseCode() != 200) {
                return -1;
            }
            try (InputStream body = conn.getInputStream()) {
                body.readAllBytes(); //응답 본문을 끝까지 받은 시점까지 측정
            }
        } catch (IOException e) { //연결 실패 등도 실패한 요청으로 센다
            return -1;
        }

        return (System.nanoTime() - start) / 1_000_000.0;
    }
}
