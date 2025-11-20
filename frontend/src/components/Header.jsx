import React from "react";
import { Star, Search, Moon, Sun } from "lucide-react";

function Header({
                    search,
                    onSearch,
                    showMyBookMarks,
                    toggleShowMyBookMarks,
                    darkMode,
                    toggleDark,
                }) {
    return (
        <div className="flex items-center justify-between px-3 py-2 border-b dark:border-gray-700">

            <Star //즐겨찾기 기능
                className={`cursor-pointer ${
                    showMyBookMarks //누르면 노랑 평상시 회색(boolean)
                        ? "text-yellow-400 fill-yellow-400"
                        : "text-gray-400"
                }`}
                size={25}
                onClick={toggleShowMyBookMarks} //나의 즐겨찾기 on/off
            />


            <div className="flex items-center bg-gray-200 dark:bg-gray-800 rounded-md pr-6 pl-2">
                <Search size={16} className="text-gray-600" />
                <input
                    type="text"
                    aria-label="코인 검색 입력"
                    placeholder="코인을 검색하세요."
                    value={search}
                    onChange={(e) => onSearch(e.target.value)}
                    className="bg-transparent outline-none text-sm pr-2 text-gray-800 dark:text-gray-100 w-32"
                />
            </div>

            {darkMode ? (
                <Sun
                    onClick={toggleDark}
                    className="cursor-pointer text-yellow-400"
                    size={25}
                />
            ) : (
                <Moon
                    onClick={toggleDark}
                    className="cursor-pointer text-gray-500"
                    size={25}
                />
            )}
        </div>
    );
}

export default Header;
