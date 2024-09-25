package com.seugi.api.domain.oauth.port.out

import com.seugi.api.domain.oauth.application.model.OAuth

interface LoadOAuthPort {

    fun loadOAuthByMemberIdAndProvider(memberId: Long, provider: String): OAuth
    fun loadOAuthByProviderAndSub(provider: String, sub: String): OAuth

}