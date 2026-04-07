package com.pokergame.common.model.game.doudizhu;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.model.CommonRep;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class GrabLandlordResp extends CommonRep {

    /** 地主ID */
    private long landlordId;

    /** 倍数 */
    private int multiple;

    /** 是否抢地主成功 */
    private boolean grabSuccess;
}
