package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.Credit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CreditRepository : JpaRepository<Credit, Long> {
    fun findByUserId(userId: Long): Optional<Credit>
} 