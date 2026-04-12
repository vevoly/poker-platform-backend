package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;

import java.util.List;

@Data
@ProtobufClass
public class GetCurrencyLogResp {

    /** 币种日志列表 */
    private List<CurrencyChangeLogDTO> records;

    /** 总数 */
    private Long total;

    /**  当前页码 */
    private Integer page;

    /**  每页数量 */
    private Integer size;
}
