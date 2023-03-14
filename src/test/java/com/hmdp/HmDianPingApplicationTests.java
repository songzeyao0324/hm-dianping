package com.hmdp;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class HmDianPingApplicationTests {
    @Test
    public void test01(){
        LocalDateTime now = LocalDateTime.now();
        TestTime testTime = new TestTime();
        testTime.setTime(now);
        System.out.println(JSONUtil.toJsonStr(testTime));


    }
}
@Data
class TestTime{
    private LocalDateTime time;
}
