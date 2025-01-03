package com.seugi.api.domain.notification.presentation.controller

import com.seugi.api.domain.notification.presentation.dto.request.CreateNotificationRequest
import com.seugi.api.domain.notification.presentation.dto.request.NotificationEmojiRequest
import com.seugi.api.domain.notification.presentation.dto.request.UpdateNotificationRequest
import com.seugi.api.domain.notification.presentation.dto.response.NotificationResponse
import com.seugi.api.domain.notification.service.NotificationService
import com.seugi.api.global.common.annotation.GetAuthenticatedId
import com.seugi.api.global.response.BaseResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notification")
class NotificationController(
    private val noticeService: NotificationService,
) {

    @PostMapping
    fun createNotice(
        @RequestBody createNoticeRequest: CreateNotificationRequest,
        @GetAuthenticatedId userId: Long,
    ): BaseResponse<Unit> {
        noticeService.createNotice(createNoticeRequest = createNoticeRequest, userId = userId)
        return BaseResponse(
            message = "공지 등록 성공"
        )
    }

    @GetMapping("/{workspaceId}")
    fun getNotices(
        @PathVariable workspaceId: String,
        @GetAuthenticatedId userId: Long,
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC, size = 20) pageable: Pageable,
    ): BaseResponse<List<NotificationResponse>> {

        return BaseResponse(
            message = "공지 불러오기 성공",
            data = noticeService.getNotices(
                workspaceId = workspaceId,
                userId = userId,
                pageable = pageable
            )
        )
    }

    @PatchMapping
    fun updateNotice(
        @RequestBody updateNoticeRequest: UpdateNotificationRequest,
        @GetAuthenticatedId userId: Long,
    ): BaseResponse<Unit> {
        noticeService.updateNotice(updateNoticeRequest = updateNoticeRequest, userId = userId)
        return BaseResponse(
            message = "공지 수정 성공"
        )
    }

    @DeleteMapping("/{workspaceId}/{id}")
    fun deleteNotice(
        @PathVariable workspaceId: String,
        @PathVariable id: Long,
        @GetAuthenticatedId userId: Long,
    ): BaseResponse<Unit> {
        noticeService.deleteNotice(id = id, workspaceId = workspaceId, userId = userId)
        return BaseResponse(
            message = "공지 삭제 성공"
        )
    }

    @PatchMapping("/emoji")
    fun addEmoji(
        @GetAuthenticatedId userId: Long,
        @RequestBody notificationEmojiRequest: NotificationEmojiRequest,
    ): BaseResponse<Unit> {
        noticeService.toggleEmoji(userId, notificationEmojiRequest)
        return BaseResponse(
            message = "성공"
        )
    }


}