package kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
} 