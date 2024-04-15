//package seugi.api.domain.chat.presentation.websocket.controller
//
//import org.springframework.messaging.handler.annotation.MessageMapping
//import org.springframework.messaging.simp.SimpAttributesContextHolder
//import org.springframework.messaging.simp.SimpMessagingTemplate
//import org.springframework.stereotype.Controller
//import seugi.api.domain.chat.application.service.message.MessageService
//import seugi.api.domain.chat.domain.chat.model.Message
//import seugi.api.domain.chat.presentation.websocket.dto.ChatMessageDto
//
//@Controller
//class StompChatController(
//    val template: SimpMessagingTemplate,
//    private val messageService: MessageService
//) {
//    @MessageMapping("/chat/message")
//    fun message(
//        message: ChatMessageDto
//    ) {
//        val simpAttributes = SimpAttributesContextHolder.currentAttributes()
//        val userId = simpAttributes.getAttribute("user-id") as String?
//
//        if (userId != null) {
//            val domainMessage: Message = messageService.saveMessage(message, userId.toLong())
//            template.convertAndSend("/sub/chat/room/" + domainMessage.chatRoomId, domainMessage)
//        }
//
//    }
//}