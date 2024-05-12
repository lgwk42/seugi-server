package com.seugi.api.domain.chat.application.service.message

import com.seugi.api.domain.chat.domain.chat.MessageEntity
import com.seugi.api.domain.chat.domain.chat.MessageRepository
import com.seugi.api.domain.chat.domain.chat.embeddable.Emoji
import com.seugi.api.domain.chat.domain.chat.mapper.MessageMapper
import com.seugi.api.domain.chat.domain.chat.model.Message
import com.seugi.api.domain.chat.domain.chat.model.Type
import com.seugi.api.domain.chat.domain.enums.status.ChatStatusEnum
import com.seugi.api.domain.chat.domain.joined.JoinedRepository
import com.seugi.api.domain.chat.domain.room.info.RoomInfoEntity
import com.seugi.api.domain.chat.domain.room.info.RoomInfoRepository
import com.seugi.api.domain.chat.exception.ChatErrorCode
import com.seugi.api.domain.chat.presentation.joined.dto.response.GetMessageResponse
import com.seugi.api.domain.chat.presentation.websocket.dto.ChatMessageDto
import com.seugi.api.domain.chat.presentation.websocket.dto.MessageEventDto
import com.seugi.api.domain.member.adapter.out.repository.MemberRepository
import com.seugi.api.global.exception.CustomException
import com.seugi.api.global.response.BaseResponse
import org.bson.types.ObjectId
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val memberRepository: MemberRepository,
    private val messageMapper: MessageMapper,
    private val joinedRepository: JoinedRepository,
    private val roomInfoRepository: RoomInfoRepository,
    private val rabbitTemplate: RabbitTemplate
) : MessageService {

    @Transactional
    override fun sendMessage(chatMessageDto: ChatMessageDto, userId: Long) {
        rabbitTemplate.convertAndSend(
            "chat.exchange", "room.${chatMessageDto.roomId}", savaMessage(chatMessageDto, userId)
        )
    }

    @Transactional
    override fun toMessage(type: Type, chatRoomId: Long, eventList: MutableList<Long>, userId: Long) {
        sendMessage(
            ChatMessageDto(
                type = type,
                roomId = chatRoomId,
                message = "",
                eventList = eventList
            ),
            userId
        )
    }

    @Transactional
    override fun savaMessage(chatMessageDto: ChatMessageDto, userId: Long): Message {

        val joinedEntity = joinedRepository.findByChatRoomId(chatMessageDto.roomId!!)

        val memberEntity = memberRepository.findById(userId)
            .orElseThrow { CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND) }

        val readUser = roomInfoRepository.findByRoomId(chatMessageDto.roomId.toString()).orEmpty()
        val readUsers = readUser.map { it.userId }

        return messageMapper.toDomain(
            messageRepository.save(
                messageMapper.toEntity(
                    messageMapper.toMessage(
                        chatMessageDto = chatMessageDto,
                        author = memberEntity,
                        joinedEntity = joinedEntity,
                        readUsers = readUsers
                    )
                )
            )
        )

    }

    @Transactional(readOnly = true)
    override fun getMessages(chatRoomId: Long, userId: Long, pageable: Pageable): BaseResponse<GetMessageResponse> {

        if (!joinedRepository.findByChatRoomId(chatRoomId).joinedUserId.contains(userId)) throw CustomException(ChatErrorCode.NO_ACCESS_ROOM)
        val allMessages = messageRepository.findByChatRoomIdEquals(chatRoomId, pageable).map { messageMapper.toDomain(it) }

        val unreadMessages: List<MessageEntity> =
            messageRepository.findByChatRoomIdEqualsAndReadNot(chatRoomId, setOf(userId))

        if (unreadMessages.isNotEmpty()) {
            unreadMessages.map { it.read.add(userId) }

            messageRepository.saveAll(unreadMessages).last()

            return BaseResponse(
                status = HttpStatus.OK.value(),
                success = true,
                state = "M1",
                message = "채팅 불러오기 성공",
                data = GetMessageResponse(
                    firstMessageId = unreadMessages.first().id?.toString(),
                    messages = allMessages
                )
            )
        } else {
            return BaseResponse(
                status = HttpStatus.OK.value(),
                success = true,
                state = "M1",
                message = "채팅 불러오기 성공",
                data = GetMessageResponse(
                    firstMessageId = if (allMessages.isEmpty()) null else allMessages.last().id,
                    messages = allMessages
                )
            )
        }
    }

    @Transactional
    override fun addEmojiToMessage(userId: Long, messageId: String, emoji: Emoji): BaseResponse<Unit> {
        val id = ObjectId(messageId)
        val message: MessageEntity = messageRepository.findById(id).get()

        message.emojiList.firstOrNull { it.emojiId == emoji.emojiId }?.userId?.add(userId)

        messageRepository.save(message)

        return BaseResponse(
            status = HttpStatus.OK.value(),
            state = "M1",
            success = true,
            message = "이모지 추가 성공"
        )
    }

    @Transactional
    override fun deleteMessage(userId: Long, messageId: String): BaseResponse<Unit> {
        val id = ObjectId(messageId)
        val message: MessageEntity = messageRepository.findById(id).get()

        if (message.author.id == userId) {
            message.messageStatus = ChatStatusEnum.DELETE
            messageRepository.save(message)
        } else {
            throw CustomException(ChatErrorCode.NO_ACCESS_MESSAGE)
        }

        return BaseResponse(
            status = HttpStatus.OK.value(),
            state = "M1",
            success = true,
            message = "메시지 지워짐으로 상태변경 성공"
        )
    }

    @Transactional
    override fun sub(userId: Long, roomId: String) {
        if (roomId != "message") {
            toMessage(
                type = Type.SUB,
                chatRoomId = roomId.toLong(),
                eventList = listOf(userId).toMutableList(),
                userId = userId
            )
            roomInfoRepository.save(
                RoomInfoEntity(
                    userId = userId,
                    roomId = roomId
                )
            )
        }
    }

    @Transactional
    override fun unsub(userId: Long) {
        val roomId = roomInfoRepository.findById(userId).get().roomId
        toMessage(
            type = Type.UNSUB,
            chatRoomId = roomId.toLong(),
            eventList = listOf(userId).toMutableList(),
            userId = userId
        )
        roomInfoRepository.deleteById(userId)
    }

}