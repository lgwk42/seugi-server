package seugi.server.domain.member.application.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import seugi.server.domain.member.adapter.`in`.dto.LoginMemberDTO
import seugi.server.domain.member.application.model.Member
import seugi.server.domain.member.port.`in`.LoginMemberUseCase
import seugi.server.domain.member.port.out.LoadMemberPort
import seugi.server.global.auth.jwt.JwtInfo
import seugi.server.global.auth.jwt.JwtUtils
import seugi.server.global.exception.CustomErrorCode
import seugi.server.global.exception.CustomException
import seugi.server.global.response.BaseResponse

@Service
class LoginMemberService (
    private val jwtUtils: JwtUtils,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val loadMemberPort: LoadMemberPort
) : LoginMemberUseCase {

    override fun loginMember(memberDTO: LoginMemberDTO): BaseResponse<JwtInfo> {
        val member: Member = loadMemberPort.loadMemberWithEmail(memberDTO.email)

        if (!bCryptPasswordEncoder.matches(memberDTO.password, member.password.value)) {
            throw CustomException(CustomErrorCode.JWT_MEMBER_NOT_MATCH)
        }

        return BaseResponse (
            code = 200,
            success = true,
            message = "토큰 발급 성공 !!",
            data = arrayListOf(
                jwtUtils.generate(
                    member
                )
            )
        )
    }
}