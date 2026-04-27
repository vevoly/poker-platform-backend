package com.pokergame.common.util;

import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.iohao.game.common.kit.ProtoKit;
import com.pokergame.common.model.broadcast.BaseBroadcastData;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 签名工具类（HMAC-SHA256）
 */
/**
 * 签名工具类（HMAC-SHA256），无状态
 * 调用方需自行管理密钥
 */
@Slf4j
public final class SignUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private SignUtil() {}

    /**
     * 计算广播数据的 HMAC-SHA256 签名
     *
     * @param data  广播数据（计算时会临时清空其 sign 字段）
     * @param key   密钥（字节数组）
     * @return 签名字符串（十六进制）
     */
    public static String sign(BaseBroadcastData data, byte[] key) {
        try {
            // 1. 暂存原签名并清除
            String oldSign = data.getSign();
            data.setSign(null);

            // 2. 序列化数据（Protobuf 按字段编号排序）
            // 使用 ProtoKit 序列化
            byte[] bytes = ProtoKit.toBytes(data);

            // 3. 计算 HMAC
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key, HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(bytes);

            // 4. 恢复原签名
            data.setSign(oldSign);

            return bytesToHex(hmac);
        } catch (Exception e) {
            log.error("签名计算失败", e);
            return "";
        }
    }

    /**
     * 验证广播数据签名
     *
     * @param data  广播数据（应包含 sign 字段）
     * @param signature 待验证的签名
     * @param key   密钥（与签名时相同）
     * @return true 有效
     */
    public static boolean verify(BaseBroadcastData data, String signature, byte[] key) {
        String expected = sign(data, key);
        return expected.equals(signature);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
