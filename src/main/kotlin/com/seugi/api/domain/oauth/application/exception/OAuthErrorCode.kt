package com.seugi.api.domain.oauth.application.exception

import com.seugi.api.global.exception.CustomErrorCode
import org.springframework.http.HttpStatus

enum class OAuthErrorCode (
    override val status: HttpStatus,
    override val state: String,
    override val message: String,
) : CustomErrorCode {

    OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "연동이 발견되지 않았습니다"),
    OAUTH_ALREADY_EXIST(HttpStatus.CONFLICT, "CONFLICT", "이미 연동되었습니다"),
    OAUTH_REFRESH_EXPIRED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "다시 구글 연동을 해주세요")

}