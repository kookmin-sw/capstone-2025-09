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
        if (userRepository.findByUsername(request.username) != null) {
            throw IllegalArgumentException("이미 존재하는 사용자 이름입니다")
        }

        val user = User(
            username = request.username,
            password = request.password, // 실제 구현시 암호화 필요
            email = request.email
        )
        
        return userRepository.save(user).id
    }
} 