package kr.ac.kookmin.cs.capstone.voicepack_platform.user

import jakarta.servlet.http.HttpSession
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserLoginRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserSignupRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun signup(request: UserSignupRequest): Long {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다")
        }

        val user = User(
            email = request.email,
            password = request.password // 실제 구현시 암호화 필요
        )
        
        return userRepository.save(user).id
    }

    fun login(request: UserLoginRequest, session: HttpSession) {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("이메일 또는 비밀번호가 틀렸습니다.")

        if (user.password != request.password) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 틀렸습니다.")
        }

        session.invalidate()  // 기존 세션을 무효화
        session.setAttribute("userId", user.id)
    }


} 