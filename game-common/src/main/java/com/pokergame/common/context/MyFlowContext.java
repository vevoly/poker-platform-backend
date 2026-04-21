package com.pokergame.common.context;

import com.iohao.game.action.skeleton.core.flow.FlowContext;

import java.util.Objects;

/**
 * 自定义 FlowContext，提供便捷方法获取用户信息
 */
public class MyFlowContext extends FlowContext {

    private MyAttachment attachment;

    /**
     * 获取附件（带缓存）
     */
    @Override
    public MyAttachment getAttachment() {
        if (Objects.isNull(attachment)) {
            this.attachment = this.getAttachment(MyAttachment.class);
        }
        return this.attachment;
    }

    /**
     * 获取用户昵称
     */
    public String getNickname() {
        MyAttachment att = getAttachment();
        return att != null ? att.getNickname() : null;
    }

    /**
     * 获取用户ID
     */
    public long getUserId() {
        MyAttachment att = getAttachment();
        return att != null ? att.getUserId() : 0L;
    }

    /**
     * 获取头像
     */
    public String getAvatar() {
        MyAttachment att = getAttachment();
        return att != null ? att.getAvatar() : null;
    }

    /**
     * 更新用户信息（同步）
     */
    public void updateUserInfo(MyAttachment newAttachment) {
        this.attachment = newAttachment;
        this.updateAttachment(newAttachment);
    }

    /**
     * 更新用户信息（异步）
     */
    public void updateUserInfoAsync(MyAttachment newAttachment) {
        this.attachment = newAttachment;
        this.updateAttachmentAsync(newAttachment);
    }
}
