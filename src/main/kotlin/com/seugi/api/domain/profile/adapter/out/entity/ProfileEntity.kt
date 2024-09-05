package com.seugi.api.domain.profile.adapter.out.entity

import com.seugi.api.domain.member.adapter.out.entity.MemberEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ProfileEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    var member: MemberEntity,

    var workspaceId : String,

    var schGrade: Int = 0, // 학년

    var schClass: Int = 0, // 반

    var schNumber: Int = 0, // 번호

    var status: String = "", // 상태메시지

    var nick: String = "", // 닉네임

    var spot: String = "", // 직위

    var belong: String = "", // 소속

    var phone: String = "", // 휴대전화번호

    var wire: String = "", // 유선전화번호

    var location: String = "", // 근무위치

)