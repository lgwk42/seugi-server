package com.seugi.api.domain.member.application.model

import com.seugi.api.domain.member.application.model.value.*

data class Member (

    val id: MemberId?,
    var name: MemberName,
    val email: MemberEmail,
    val password: MemberPassword,
    var birth: MemberBirth,
    var profile: MemberProfile,
    val role: MemberRole,
    val loginId: MemberLoginId,
    val provider: MemberProvider,
    val providerId: MemberProviderId

)