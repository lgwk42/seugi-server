package com.seugi.api.global.infra.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.seugi.api.domain.chat.domain.chat.model.Type
import com.seugi.api.domain.chat.domain.room.ChatRoomEntity
import com.seugi.api.domain.chat.domain.room.ChatRoomRepository
import com.seugi.api.domain.chat.exception.ChatErrorCode
import com.seugi.api.domain.member.application.model.Member
import com.seugi.api.domain.member.application.port.out.LoadMemberPort
import com.seugi.api.domain.workspace.domain.entity.WorkspaceEntity
import com.seugi.api.global.exception.CustomException
import com.seugi.api.global.util.DateTimeUtil
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FCMService(
    private val memberPort: LoadMemberPort,
    private val chatRoomRepository: ChatRoomRepository,
    @Value("\${fcm.icon.url}") private val icon: String,
) {

    private fun getMember(userId: Long): Member {
        return memberPort.loadMemberWithId(userId)
    }

    private fun Member.getImg(): String? {
        return this.picture.value
    }

    private fun Member.getFCMToken(): Set<String> {
        return this.fcmToken.token
    }

    private fun getAlarmImage(workspace: WorkspaceEntity?, type: FCMEnums, member: Member): String? {
        return when (type) {
            FCMEnums.CHAT -> member.getImg()
            FCMEnums.NOTIFICATION -> workspace?.workspaceImageUrl ?: icon
        }
    }

    private fun buildNotification(title: String, body: String, imageUrl: String?): Notification {
        return Notification.builder()
            .setTitle(title)
            .setBody(body)
            .setImage(imageUrl ?: icon)
            .build()
    }

    private fun sendFCMNotifications(tokens: Set<String>, notification: Notification) {
        tokens.forEach { token ->
            FirebaseMessaging.getInstance().sendAsync(
                Message.builder()
                    .setToken(token.ifEmpty { return })
                    .setNotification(notification)
                    .build()
            )
        }
    }

    private fun findChatRoomById(id: String): ChatRoomEntity {
        if (id.length != 24) throw CustomException(ChatErrorCode.CHAT_ROOM_ID_ERROR)
        return chatRoomRepository.findById(ObjectId(id))
            .orElseThrow { CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND) }
    }

    fun sendChatAlarm(message: com.seugi.api.domain.chat.domain.chat.model.Message, chatRoomId: String, userId: Long) {

        val room = findChatRoomById(chatRoomId)

        val sendMember = getMember(userId)

        if (message.message.isEmpty()) return

        val notification = buildNotification(
            title = room.chatName.ifEmpty { "1대1 채팅" },
            body = "${sendMember.name.value} : ${if (message.type == Type.FILE) "파일을 보냈습니다." else if (message.type == Type.IMG) "사진을 보냈습니다." else message.message}",
            imageUrl = getAlarmImage(workspace = null, type = FCMEnums.CHAT, member = sendMember)
        )

        room.joinedUserInfo.forEach {
            if (it.checkDate != DateTimeUtil.checkDate && it.userId != userId) {
                sendFCMNotifications(getMember(it.userId).getFCMToken(), notification)
            }
        }
    }

    fun sendNotificationAlert(workspace: WorkspaceEntity, userId: Long, message: String) {

        val sendUser = getMember(userId)
        val users = workspace.student + workspace.middleAdmin + workspace.workspaceAdmin - userId

        val notification = buildNotification(
            title = "[공지] ${workspace.workspaceName}",
            body = "${sendUser.name.value} : $message",
            imageUrl = getAlarmImage(workspace, FCMEnums.NOTIFICATION, sendUser)
        )

        users.forEach {
            sendFCMNotifications(getMember(it!!).getFCMToken(), notification)
        }

    }

    fun sendJoinWorkspaceAlert(users: Set<Long>, workspaceEntity: WorkspaceEntity) {

        val notification = buildNotification(
            title = "${workspaceEntity.workspaceName}",
            body = "워크스페이스 가입이 승인되었습니다.",
            imageUrl = workspaceEntity.workspaceImageUrl
        )

        users.forEach {
            sendFCMNotifications(getMember(it).getFCMToken(), notification)
        }
    }

}