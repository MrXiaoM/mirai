/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import net.mamoe.mirai.contact.User

/**
 * 来自 [User] 的消息
 *
 * @see FriendMessageEvent
 * @see TempMessageEvent
 */
public interface UserMessageEvent : MessageEvent {
    public override val subject: User
}