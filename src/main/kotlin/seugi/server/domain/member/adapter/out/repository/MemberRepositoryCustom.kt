package seugi.server.domain.member.adapter.out.repository

import seugi.server.domain.member.adapter.out.entity.MemberEntity
import java.util.Optional

interface MemberRepositoryCustom {

    fun findByEmail(email: String): Optional<MemberEntity>

    fun existsByEmail(email: String): Boolean

}