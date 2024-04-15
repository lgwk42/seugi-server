package com.seugi.api.domain.chat.domain.chat

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface MessageRepository :MongoRepository<MessageEntity, ObjectId>{
    fun findByChatRoomIdEquals(chatRoomId: Long) : List<MessageEntity>
    fun findByChatRoomIdEqualsAndReadNot(chatRoomId: Long, read: Set<Long>) : List<MessageEntity>
    fun findByChatRoomIdEqualsAndAuthorId(chatRoomId: Long, authorId: Long): List<MessageEntity>
}