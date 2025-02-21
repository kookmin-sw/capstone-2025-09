package kr.ac.kookmin.cs.capstone.voicepack_platform.user

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
} 