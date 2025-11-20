import React, { useState } from "react";
import { Star } from "lucide-react";

function TickerTable({ tickers, bookMarks, toggleFavorite }) {
    const [sortField, setSortField] = useState(null); //정렬기준
    const [sortOrder, setSortOrder] = useState("desc"); //정렬방향

    const formatVolume = (v) => { //거래대금 포맷
        if (v >= 1_000_000_000_000) return (v / 1_000_000_000_000).toFixed(1) + "조";
        if (v >= 1_000_000_000) return (v / 1_000_000_000).toFixed(1) + "B";
        if (v >= 1_000_000) return (v / 1_000_000).toFixed(1) + "M";
        return v.toLocaleString();
    };

    const formatChangeRate = (r, s) => { //전일대비 포맷
        const abs = Math.abs(r);
        if (s === "RISE") return `+${abs.toFixed(2)}%`; //상승
        if (s === "FALL") return `-${abs.toFixed(2)}%`; //하락
        return `${abs.toFixed(2)}%`;
    };

    const handleSort = (field) => {   //정렬 버튼 클릭시 로직
        if (sortField === field)
            setSortOrder(sortOrder === "desc" ? "asc" : "desc");
        else {
            setSortField(field);
            setSortOrder("desc");
        }
    };

    const sorted = [...tickers].sort((a, b) => { //원본 배열 수정x -> 스프레드로 tickers 배열 복사본 생성
        if (!sortField) return 0; //기본 세팅 (아무것도 정렬 안한 상태)
        const valA = a[sortField];
        const valB = b[sortField];
        return sortOrder === "desc" ? valB - valA : valA - valB; //valB - valA는 내림차순 valA - valB는 오름차순
    });

    return (
        <div className="p-2 text-sm overflow-y-auto h-[600px]">
            {/* 헤더 */}
            <div className="grid grid-cols-[110px_1fr_1fr_1fr] text-center font-semibold border-b pb-2 mb-2 dark:border-gray-700">

            <div className="text-left pl-8">마켓</div>
                <div
                    className="cursor-pointer select-none hover:text-yellow-400"
                    onClick={() => handleSort("tradePrice")}
                >
                    현재가 {sortField === "tradePrice" && (sortOrder === "desc" ? "▼" : "▲")}
                </div>
                <div
                    className="cursor-pointer select-none hover:text-yellow-400"
                    onClick={() => handleSort("changeRate")}
                >
                    전일대비 {sortField === "changeRate" && (sortOrder === "desc" ? "▼" : "▲")}
                </div>
                <div
                    className="cursor-pointer select-none hover:text-yellow-400"
                    onClick={() => handleSort("accTradePrice")}
                >
                    거래대금 {sortField === "accTradePrice" && (sortOrder === "desc" ? "▼" : "▲")}
                </div>
            </div>

            {/*본문*/}
            {sorted.length === 0 ? (
                <div className="text-center text-gray-400 py-4">코인 목록 불러오는중...</div>
            ) : (
                sorted.map((coin) => (
                    <div
                        key={coin.code}
                        className="grid grid-cols-[110px_1fr_1fr_1fr] items-center text-center py-1 border-b dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                    >
                        {/*즐겨찾기*/}
                        <div className="flex items-center space-x-2 text-left pl-2">
                            <Star
                                size={14}
                                onClick={() => toggleFavorite(coin.code)}
                                className={`w-[14px] h-[14px] shrink-0 cursor-pointer ${
                                    bookMarks.includes(coin.code)
                                        ? "text-yellow-400 fill-yellow-400"
                                        : "text-gray-400"
                                }`}
                            />
                            <div className="flex flex-col leading-tight">
                                <span className="font-medium text-[13px]">{coin.koreanName}</span>
                                <span className="text-[11px] text-gray-500 dark:text-gray-400">
                  {coin.code}
                </span>
                            </div>
                        </div>

                        {/*현재가*/}
                        <div className="font-medium text-[13px]">
                            {coin.tradePrice ? coin.tradePrice.toLocaleString() : "-"}
                        </div>

                        {/*전일대비*/}
                        <div
                            className={`font-medium text-[13px] ${
                                coin.changeState === "RISE"
                                    ? "text-red-500"
                                    : coin.changeState === "FALL"
                                        ? "text-blue-500"
                                        : "text-gray-400"
                            }`}
                        >
                            {formatChangeRate(coin.changeRate, coin.changeState)}
                        </div>

                        {/*거래대금*/}
                        <div className="text-[13px] text-gray-600 dark:text-gray-300">
                            {formatVolume(coin.accTradePrice)}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
}

export default TickerTable;
