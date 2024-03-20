package seugi.server.domain.member.application.service

import org.springframework.stereotype.Service
import seugi.server.domain.member.adapter.`in`.dto.CreateMemberDTO
import seugi.server.domain.member.application.model.Member
import seugi.server.domain.member.port.`in`.CreateMemberUseCase
import seugi.server.domain.member.port.out.SaveMemberPort
import seugi.server.global.response.BaseResponse

@Service
class CreateMemberService (
    private val saveMemberPort: SaveMemberPort
): CreateMemberUseCase {

    override fun createMember(memberDTO: CreateMemberDTO): BaseResponse<Any> {
        saveMemberPort.saveMember(Member(
            null
        ))

        return BaseResponse (
            code = 200,
            success = true,
            message = "회원가입 성공 !!",
            data = emptyList()
        )
    }

}