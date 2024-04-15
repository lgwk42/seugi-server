package com.seugi.api.domain.chat.domain.chat

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import com.seugi.api.domain.chat.domain.chat.embeddable.Emoji
import com.seugi.api.domain.chat.domain.chat.embeddable.MessageMember
import com.seugi.api.domain.chat.domain.chat.embeddable.MessageUserId
import com.seugi.api.domain.chat.domain.chat.model.Type
import com.seugi.api.domain.chat.domain.enums.status.ChatStatusEnum
import java.time.LocalDateTime


@Document(collection = "messages")
data class MessageEntity(

    @Id
    val id: ObjectId? = null,

    var chatRoomId: Long? = null,

    @Enumerated(EnumType.ORDINAL)
    val type : Type,

    var author : MessageMember,

    val message : String = "",

    val emojiList: MutableList<Emoji> = MutableList(8) { Emoji(it+1) },

    val mention : List<MessageUserId> = emptyList(),

    val mentionALl : Boolean = false,

    var timestamp: LocalDateTime = LocalDateTime.now(),

    var read: MutableSet<Long> = mutableSetOf(),

    var joined: Set<Long>,

    var messageStatus: ChatStatusEnum = ChatStatusEnum.ALIVE

)