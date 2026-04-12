package com.pokergame.user.action;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.common.model.user.*;
import com.pokergame.user.UserServerApplication;
import com.pokergame.user.converter.CurrencyChangeLogConverter;
import com.pokergame.user.converter.CurrencyConverter;
import com.pokergame.user.entity.CurrencyChangeLogEntity;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.mapper.CurrencyChangeLogMapper;
import com.pokergame.user.service.CurrencyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = UserServerApplication.class)
@ActiveProfiles("test")
@DisplayName("CurrencyAction 单元测试")
class CurrencyActionTest {

    @Autowired
    private CurrencyAction currencyAction;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private CurrencyConverter currencyConverter;

    @MockBean
    private CurrencyChangeLogConverter currencyChangeLogConverter;

    @MockBean
    private CurrencyChangeLogMapper currencyChangeLogMapper;

    // ==================== getCurrency ====================

    @Test
    @DisplayName("getCurrency - 查询所有货币成功")
    void getCurrency_AllTypes() {
        Long userId = 1001L;
        List<UserCurrencyEntity> entities = Arrays.asList(
                new UserCurrencyEntity() {{ setCurrencyType("GOLD"); setAmount(10000L); }},
                new UserCurrencyEntity() {{ setCurrencyType("DIAMOND"); setAmount(500L); }}
        );
        when(currencyService.getUserCurrencies(userId)).thenReturn(entities);

        GetCurrencyReq req = new GetCurrencyReq().setUserId(userId);
        GetCurrencyResp resp = currencyAction.getCurrency(req);

        assertNotNull(resp);
        verify(currencyService).getUserCurrencies(userId);
        verify(currencyConverter).toDTOList(entities);
    }

    @Test
    @DisplayName("getCurrency - 查询指定货币成功")
    void getCurrency_SpecificType() {
        Long userId = 1001L;
        String currencyType = "GOLD";
        UserCurrencyEntity entity = new UserCurrencyEntity() {{ setCurrencyType("GOLD"); setAmount(10000L); }};
        when(currencyService.getUserCurrency(eq(userId), any(CurrencyType.class))).thenReturn(entity);

        GetCurrencyReq req = new GetCurrencyReq().setUserId(userId).setCurrencyType(currencyType);
        GetCurrencyResp resp = currencyAction.getCurrency(req);

        assertNotNull(resp);
        verify(currencyService).getUserCurrency(eq(userId), any(CurrencyType.class));
        verify(currencyConverter).toDTOList(Collections.singletonList(entity));
    }

    @Test
    @DisplayName("getCurrency - 用户ID为空（参数校验失败）")
    void getCurrency_NullUserId() {
        GetCurrencyReq req = new GetCurrencyReq();
        assertThrows(MsgException.class, () -> currencyAction.getCurrency(req));
        verify(currencyService, never()).getUserCurrencies(anyLong());
    }

    // ==================== increase ====================

    @Test
    @DisplayName("increase - 增加货币成功")
    void increase_Success() {
        Long userId = 1001L;
        String currencyType = "GOLD";
        Long amount = 100L;
        Long afterAmount = 10100L;

        when(currencyService.increaseCurrency(
                eq(userId),
                eq(CurrencyType.GOLD),
                eq(amount),
                eq(ChangeCurrencyType.RECHARGE),
                any(),   // orderId 可以为 null
                any()    // remark 可以为 null
        )).thenReturn(afterAmount);

        IncreaseCurrencyReq req = new IncreaseCurrencyReq()
                .setUserId(userId)
                .setCurrencyType(currencyType)
                .setAmount(amount)
                .setChangeType(ChangeCurrencyType.RECHARGE.getCode());
        IncreaseCurrencyResp resp = currencyAction.increase(req);

        assertNotNull(resp);
        assertEquals(afterAmount, resp.getAfterAmount());
        verify(currencyService).increaseCurrency(
                eq(userId), eq(CurrencyType.GOLD), eq(amount),
                eq(ChangeCurrencyType.RECHARGE), isNull(), isNull()
        );
    }

    @Test
    @DisplayName("increase - 参数校验失败（amount为null）")
    void increase_NullAmount() {
        IncreaseCurrencyReq req = new IncreaseCurrencyReq()
                .setUserId(1001L)
                .setCurrencyType("GOLD")
                .setChangeType("RECHARGE");
        assertThrows(MsgException.class, () -> currencyAction.increase(req));
        verify(currencyService, never()).increaseCurrency(any(), any(), any(), any(), any(), any());
    }

    // ==================== decrease ====================

    @Test
    @DisplayName("decrease - 减少货币成功")
    void decrease_Success() {
        Long userId = 1001L;
        String currencyType = "GOLD";
        Long amount = 50L;
        Long afterAmount = 9950L;

        when(currencyService.decreaseCurrency(
                eq(userId),
                eq(CurrencyType.GOLD),
                eq(amount),
                eq(ChangeCurrencyType.GAME_LOSE),
                any(),
                any()
        )).thenReturn(afterAmount);

        DecreaseCurrencyReq req = new DecreaseCurrencyReq()
                .setUserId(userId)
                .setCurrencyType(currencyType)
                .setAmount(amount)
                .setChangeType(ChangeCurrencyType.GAME_LOSE.getCode());
        DecreaseCurrencyResp resp = currencyAction.decrease(req);

        assertNotNull(resp);
        assertEquals(afterAmount, resp.getAfterAmount());
    }

    // ==================== getLog ====================

    @Test
    @DisplayName("getLog - 查询流水成功")
    void getLog_Success() {
        Long userId = 1001L;
        int page = 1, size = 10;

        // 准备 Mock 数据
        Page<CurrencyChangeLogEntity> mockPage = new Page<>(page, size);
        mockPage.setTotal(2L);
        mockPage.setRecords(Arrays.asList(
                new CurrencyChangeLogEntity(),
                new CurrencyChangeLogEntity()
        ));

        // Mock selectPage 返回值
        when(currencyChangeLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // Mock 转换器
        when(currencyChangeLogConverter.toDTOList(anyList()))
                .thenReturn(Collections.emptyList());

        GetCurrencyLogReq req = new GetCurrencyLogReq()
                .setUserId(userId)
                .setPage(page)
                .setSize(size);

        GetCurrencyLogResp resp = currencyAction.getLog(req);

        assertNotNull(resp);
        assertNotNull(resp.getRecords());
        verify(currencyChangeLogMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("getLog - 参数校验失败（page为0）")
    void getLog_InvalidPage() {
        GetCurrencyLogReq req = new GetCurrencyLogReq()
                .setUserId(1001L)
                .setPage(0);
        assertThrows(MsgException.class, () -> currencyAction.getLog(req));
        verify(currencyChangeLogMapper, never()).selectPage(any(), any());
    }
}
