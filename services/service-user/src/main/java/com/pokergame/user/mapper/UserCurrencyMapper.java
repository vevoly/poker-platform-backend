package com.pokergame.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pokergame.user.entity.UserCurrencyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户货币 Mapper 接口
 *
 * <p>使用乐观锁保证并发安全，所有更新操作都需要传入 version
 *
 * @author poker-platform
 */
@Mapper
public interface UserCurrencyMapper extends BaseMapper<UserCurrencyEntity> {

    /**
     * 查询用户所有货币
     *
     * @param userId 用户ID
     * @return 货币列表
     */
    @Select("SELECT * FROM user_currency WHERE user_id = #{userId}")
    List<UserCurrencyEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户指定货币
     *
     * @param userId       用户ID
     * @param currencyType 货币类型
     * @return 货币实体
     */
    @Select("SELECT * FROM user_currency WHERE user_id = #{userId} AND currency_type = #{currencyType}")
    UserCurrencyEntity selectByUserIdAndType(@Param("userId") Long userId,
                                             @Param("currencyType") String currencyType);

    /**
     * 查询用户货币（带行锁）
     */
    @Select("SELECT * FROM user_currency WHERE user_id = #{userId} AND currency_type = #{currencyType} FOR UPDATE")
    UserCurrencyEntity selectByUserIdAndTypeForUpdate(@Param("userId") Long userId,
                                                      @Param("currencyType") String currencyType);

    /**
     * 增加货币数量（乐观锁）
     *
     * <p>使用乐观锁保证并发安全，通过 version 字段防止并发覆盖
     *
     * @param userId       用户ID
     * @param currencyType 货币类型
     * @param increment    增加数量（正数）
     * @param version      当前版本号
     * @return 影响行数（0表示乐观锁冲突，需要重试）
     */
    @Update("UPDATE user_currency SET amount = amount + #{increment}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId} " +
            "AND currency_type = #{currencyType} " +
            "AND version = #{version}")
    int increaseAmount(@Param("userId") Long userId,
                       @Param("currencyType") String currencyType,
                       @Param("increment") Long increment,
                       @Param("version") Integer version);

    /**
     * 减少货币数量（乐观锁 + 余额检查）
     *
     * @param userId       用户ID
     * @param currencyType 货币类型
     * @param decrement    减少数量（正数）
     * @param version      当前版本号
     * @return 影响行数（0表示余额不足或乐观锁冲突）
     */
    @Update("UPDATE user_currency SET amount = amount - #{decrement}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId} " +
            "AND currency_type = #{currencyType} " +
            "AND version = #{version} " +
            "AND amount >= #{decrement}")
    int decreaseAmount(@Param("userId") Long userId,
                       @Param("currencyType") String currencyType,
                       @Param("decrement") Long decrement,
                       @Param("version") Integer version);

    /**
     * 批量查询用户货币（用于批量获取）
     *
     * @param userIds 用户ID列表
     * @return 货币列表
     */
    List<UserCurrencyEntity> selectBatchByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 批量插入用户初始货币（注册时使用）
     *
     * @param list 货币列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<UserCurrencyEntity> list);
}
