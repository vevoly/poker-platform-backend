package com.pokergame.common.model;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class CommonRep {
    /** 是否成功 */
    private boolean success;

    /** 错误码（成功时为0） */
    private int errorCode;

    /** 错误信息 */
    private String errorMessage;

    public static CommonRep success() {
        return new CommonRep().setSuccess(true).setErrorCode(0);
    }

    public static CommonRep failure(int errorCode, String errorMessage) {
        return new CommonRep()
                .setSuccess(false)
                .setErrorCode(errorCode)
                .setErrorMessage(errorMessage);
    }
}
