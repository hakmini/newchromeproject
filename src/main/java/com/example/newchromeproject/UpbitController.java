package com.example.newchromeproject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UpbitController {
    @GetMapping("/")
    @ResponseBody
    public String test() {
        return "Spring Boot WebSocket 서버가 정상적으로 실행 중입니다";
    }
}
