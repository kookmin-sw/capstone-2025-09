package kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.web.multipart.MultipartFile

data class UserSignupRequest(

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,

    val profileImage: MultipartFile? = null
) 