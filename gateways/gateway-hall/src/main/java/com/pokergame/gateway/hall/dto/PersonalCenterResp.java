package com.pokergame.gateway.hall.dto;

import com.pokergame.common.model.user.UserCurrencyDTO;
import com.pokergame.common.model.user.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * 用户中心聚合 DTO
 */
@Data
public class PersonalCenterResp {
    private UserDTO user;
    private List<UserCurrencyDTO> currencies;
}
