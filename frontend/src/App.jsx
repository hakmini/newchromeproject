import React, { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import Header from "./components/Header";
import TickerTable from "./components/TickerTable";

function App() {
    const [tickers, setTickers] = useState([]);
    const [darkMode, setDarkMode] = useState(false);
    const [search, setSearch] = useState("");
    const [bookMarks, setBookMarks] = useState(() =>
        JSON.parse(localStorage.getItem("bookMarks") || "[]")
    ); // 즐겨찾기 리스트
    const [showMyBookMarks, setShowMyBookMarks] = useState(false);

    //0원 방지 초기 시세 로딩 로직
    useEffect(() => { // 컴포넌트가 처음 렌더딩 될 때, 또는 특정 값이 바뀔 때 useEffect를 사용
        async function initData() { //비동기처리 + 예외처리까지(catch)
            try {
                const pairsRes = await fetch("https://api.upbit.com/v1/market/all");
                const convertPairs = await pairsRes.json();
                const krwFilter = convertPairs.filter((m) => m.market.startsWith("KRW-")); //KRW로 시작하는 페어 목록 추출(배열형태)

                const limitTickersSize = 100; //시세 데이터(ticker) 최대 요청 100회
                const allTickers = [];
                for (let i = 0; i < krwFilter.length; i += limitTickersSize) { //초기 시세 불러오기 로직(100번씩 끊어서)
                    const marketSlice = krwFilter.slice(i, i + limitTickersSize);
                    const codes = marketSlice.map((m) => m.market).join(",");
                    const tickerRes = await fetch(
                        `https://api.upbit.com/v1/ticker?markets=${codes}` //KRW로 시작하는 현재가 데이터 목록 추출(배열형태)
                    );
                    const convertTicker = await tickerRes.json();
                    allTickers.push(...convertTicker);
                } //초기 시세 데이터 불러오기 완

                const merged = krwFilter.map((m) => { //pairs + tickers 데이터 병합
                    const t = allTickers.find((x) => x.market === m.market); //각 api데이터의 마켓정보 매칭코드
                    return {
                        code: m.market,
                        koreanName: m.korean_name,
                        englishName: m.english_name,
                        tradePrice: t?.trade_price || 0,
                        changeRate: (t?.signed_change_rate ?? 0) * 100,
                        changeState: t?.change || "EVEN",
                        accTradePrice: t?.acc_trade_price_24h || 0,
                    };
                });

                setTickers(merged);
            } catch (e) {
                console.error("초기 데이터(시세) 로딩 실패:", e);
            }
        }
        initData();
    }, []);


    // 2️⃣ WebSocket(spring server) 연결
    useEffect(() => {
        const stompClient = new Client({
            webSocketFactory: () => new SockJS("https://newchromeproject.onrender.com/coinprice"),//웹소켓 연결은 스프링서버(render)를 통해 연결
            reconnectDelay: 5000, //delay 5초 설정
            onConnect: () => { //연결 뒤 로직을 담당하는 콜백함수
                stompClient.subscribe("/topic/upbit", (message) => { //message는 서버가 클라이언트에게 전송한 STOMP 메시지 객체
                    const jsonMessage = JSON.parse(message.body); //message.body는 문자열 형태라서 js객체로 변환해줘야함
                    setTickers((prevTickers) => //이전 상태를 기반으로 업데이트(비동기 업데이트가 자주 일어나기 때문에)
                        prevTickers.map((t) => (t.code === jsonMessage.code ? { ...t, ...jsonMessage } : t)) //code기준으로 웹소켓에서 받아온 실시간 시세 데이터 덮어쓰기
                    );
                });
            },
        });
        stompClient.activate(); //연결 활성화
        return () => stompClient.deactivate(); //연결 종료(종료를 해주지 않으면 웹소켓 연결이 계속 살아있어 중복 연결로 메모리 누수가 발생/ 웹소켓은 비동기 연결이니까)
    }, []);

    /////////////////////////////////////////////////////// 즐겨찾기 로직 (추가 + 삭제)
    const toggleFavorite = (code) => {
        setBookMarks((prevTickers) => {
            const updated = prevTickers.includes(code) ? prevTickers.filter((c) => c !== code) : [...prevTickers, code];
            localStorage.setItem("bookMarks", JSON.stringify(updated)); // 브라우저 로컬에 데이터 내보내기[bookMarks]
            return updated;
        });
    };

    /////////////////////////////////////////////////////// 검색 로직
    const filteredTickers = tickers.filter((t) => { //검색된 코인 이름 빼고 나머지 버리기(영어, 한국어 가능)
        const keyword = search.toLowerCase();
        return (
            t.koreanName.toLowerCase().includes(keyword) ||
            t.englishName.toLowerCase().includes(keyword)
        );
    });


    const visibleTickers = showMyBookMarks //즐겨찾기에서 코인 검색로직
        ? filteredTickers.filter((t) => bookMarks.includes(t.code))
        : filteredTickers;


    return (
        <div
            className={`${
                darkMode ? "dark bg-gray-900 text-white" : "bg-white text-black"
            } w-[360px] rounded-lg shadow-lg`}
        >
            <Header //props toss(단방향 흐름 패턴)
                onSearch={setSearch} //Controlled Component pattern
                toggleDark={() => setDarkMode(!darkMode)}
                darkMode={darkMode} //boolean 값
                showMyBookMarks={showMyBookMarks}//boolean 값 별 누르면 노란색, 안누르면 회색
                toggleShowMyBookMarks={() =>
                    setShowMyBookMarks((prevTickers) => !prevTickers) //나의 즐겨찾기 보기
                }
            />
            <TickerTable
                tickers={visibleTickers}
                bookMarks={bookMarks}
                toggleFavorite={toggleFavorite}
            />
        </div>
    );
}

export default App;

