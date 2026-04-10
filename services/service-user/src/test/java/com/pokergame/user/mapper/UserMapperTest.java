package com.pokergame.user.mapper;

import com.pokergame.user.UserServerApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = UserServerApplication.class)
@ActiveProfiles("test")
@DisplayName("Mapper 简单测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("测试 Mapper 是否可以注入")
    void testMapperInjection() {
        assertNotNull(userMapper);
    }
}
