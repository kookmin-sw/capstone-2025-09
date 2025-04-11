package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.service

import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.Credit
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.CreditTransaction
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.ReferenceType
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionType
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository.CreditRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository.CreditTransactionRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class CreditService(
    private val creditRepository: CreditRepository,
    private val creditTransactionRepository: CreditTransactionRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    /**
     * 사용자의 크레딧 잔액을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getUserBalance(userId: Long): CreditBalanceDto {
        logger.info("크레딧 잔액 조회: userId={}", userId)
        
        val credit = findOrCreateUserCredit(userId)
        return CreditBalanceDto.fromEntity(credit)
    }
    
    /**
     * 크레딧을 충전합니다.
     */
    @Transactional
    fun chargeCredits(request: ChargeCreditsRequest): TransactionResultDto {
        logger.info("크레딧 충전 요청: {}", request)
        
        if (request.amount <= 0) {
            throw IllegalArgumentException("충전 금액은 양수여야 합니다")
        }
        
        val user = findUser(request.userId)
        val credit = findOrCreateUserCredit(user.id)
        val balanceBefore = credit.balance
        
        // 트랜잭션 기록 생성
        val transaction = CreditTransaction(
            user = user,
            amount = request.amount,
            type = TransactionType.CHARGE,
            description = "크레딧 충전: ${request.paymentMethod ?: "일반"}",
            status = TransactionStatus.PENDING,
            balanceBefore = balanceBefore
        )
        creditTransactionRepository.save(transaction)
        
        try {
            // 실제 충전 로직 (실제 결제 시스템 연동은 여기서 구현)
            
            // 크레딧 잔액 업데이트
            val time = OffsetDateTime.now()

            credit.balance += request.amount
            credit.updatedAt = time
            creditRepository.save(credit)
            
            // 트랜잭션 상태 업데이트
            transaction.status = TransactionStatus.COMPLETED
            transaction.balanceAfter = credit.balance
            transaction.updatedAt = time
            creditTransactionRepository.save(transaction)
            
            logger.info("크레딧 충전 성공: userId={}, amount={}, newBalance={}", 
                user.id, request.amount, credit.balance)
            
            return TransactionResultDto(
                transactionId = transaction.id,
                status = TransactionStatus.COMPLETED.name,
                newBalance = credit.balance,
                message = "크레딧 충전이 완료되었습니다"
            )
        } catch (e: Exception) {
            // 오류 발생 시 트랜잭션 상태 업데이트
            transaction.status = TransactionStatus.FAILED
            transaction.updatedAt = OffsetDateTime.now()
            creditTransactionRepository.save(transaction)
            
            logger.error("크레딧 충전 실패: userId={}, amount={}, error={}", 
                user.id, request.amount, e.message, e)
            
            throw RuntimeException("크레딧 충전 중 오류가 발생했습니다: ${e.message}", e)
        }
    }
    
    /**
     * 크레딧을 사용합니다.
     */
    @Transactional
    fun useCredits(request: UseCreditsRequest): TransactionResultDto {
        logger.info("크레딧 사용 요청: {}", request)
        
        if (request.amount <= 0) {
            throw IllegalArgumentException("사용 금액은 양수여야 합니다")
        }
        
        val user = findUser(request.userId)
        val credit = findOrCreateUserCredit(user.id)
        val balanceBefore = credit.balance
        
        // 잔액 확인
        if (balanceBefore < request.amount) {
            logger.warn("크레딧 부족: userId={}, required={}, balance={}", 
                user.id, request.amount, balanceBefore)
            
            // 실패 트랜잭션 기록
            val failedTransaction = CreditTransaction(
                user = user,
                amount = request.amount,
                type = TransactionType.PURCHASE,
                referenceId = request.referenceId,
                referenceType = request.referenceType,
                description = request.description ?: "크레딧 사용 시도",
                status = TransactionStatus.FAILED,
                balanceBefore = balanceBefore,
                balanceAfter = balanceBefore // 잔액 변동 없음
            )
            // 별도 트랜잭션으로 저장하여 롤백 방지
            creditTransactionRepository.saveAndFlush(failedTransaction)
            
            // 결과 DTO 반환 (트랜잭션 롤백되지 않음)
            return TransactionResultDto(
                transactionId = failedTransaction.id,
                status = TransactionStatus.FAILED.name,
                newBalance = balanceBefore,
                message = "크레딧이 부족합니다 (필요: ${request.amount}, 보유: $balanceBefore)"
            )
        }
        
        // 트랜잭션 기록 생성
        val transaction = CreditTransaction(
            user = user,
            amount = request.amount,
            type = TransactionType.PURCHASE,
            referenceId = request.referenceId,
            referenceType = request.referenceType,
            description = request.description ?: "크레딧 사용",
            status = TransactionStatus.PENDING,
            balanceBefore = balanceBefore
        )
        creditTransactionRepository.save(transaction)
        
        try {
            // 크레딧 잔액 업데이트
            val time = OffsetDateTime.now()

            credit.balance -= request.amount
            credit.updatedAt = time
            creditRepository.save(credit)
            
            // 트랜잭션 상태 업데이트
            transaction.status = TransactionStatus.COMPLETED
            transaction.balanceAfter = credit.balance
            transaction.updatedAt = time
            creditTransactionRepository.save(transaction)
            
            logger.info("크레딧 사용 성공: userId={}, amount={}, newBalance={}, referenceType={}, referenceId={}", 
                user.id, request.amount, credit.balance, request.referenceType, request.referenceId)
            
            return TransactionResultDto(
                transactionId = transaction.id,
                status = TransactionStatus.COMPLETED.name,
                newBalance = credit.balance,
                message = "크레딧 사용이 완료되었습니다"
            )
        } catch (e: Exception) {
            // 오류 발생 시 트랜잭션 상태 업데이트
            transaction.status = TransactionStatus.FAILED
            transaction.updatedAt = OffsetDateTime.now()
            creditTransactionRepository.save(transaction)
            
            logger.error("크레딧 사용 실패: userId={}, amount={}, error={}", 
                user.id, request.amount, e.message, e)
            
            throw RuntimeException("크레딧 사용 중 오류가 발생했습니다: ${e.message}", e)
        }
    }
    
    /**
     * 크레딧 거래 내역을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getTransactionHistory(request: GetTransactionsRequest): Page<CreditTransactionDto> {
        logger.info("크레딧 거래 내역 조회: {}", request)
        
        val pageable = PageRequest.of(request.page, request.size)
        
        // 조회 조건에 따라 다른 메서드 호출
        val transactions = when {
            // 날짜 범위로 필터링
            request.startDate != null && request.endDate != null -> {
                val startDate = OffsetDateTime.parse(request.startDate)
                val endDate = OffsetDateTime.parse(request.endDate)
                creditTransactionRepository.findByUserIdAndCreatedAtBetween(
                    request.userId, startDate, endDate, pageable
                )
            }
            // 타입으로 필터링
            request.type != null -> {
                val type = TransactionType.valueOf(request.type)
                creditTransactionRepository.findByUserIdAndType(
                    request.userId, type, pageable
                )
            }
            // 상태로 필터링
            request.status != null -> {
                val status = TransactionStatus.valueOf(request.status)
                creditTransactionRepository.findByUserIdAndStatus(
                    request.userId, status, pageable
                )
            }
            // 모든 거래 내역
            else -> {
                creditTransactionRepository.findByUserId(request.userId, pageable)
            }
        }
        
        return transactions.map { CreditTransactionDto.fromEntity(it) }
    }
    
    /**
     * 사용자 크레딧 정보를 찾거나, 없으면 생성합니다.
     */
    @Transactional
    fun findOrCreateUserCredit(userId: Long): Credit {
        val user = findUser(userId)
        
        return creditRepository.findByUserId(userId) ?: creditRepository.save(Credit(user = user, balance = 0))
    }
    
    /**
     * 사용자 정보를 조회합니다.
     */
    private fun findUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
    }
    
    /**
     * 크레딧을 현금으로 환전합니다.
     */
    @Transactional
    fun exchangeCredits(request: ExchangeCreditsRequest): TransactionResultDto {
        logger.info("크레딧 환전 요청: {}", request)
        
        if (request.amount <= 0) {
            throw IllegalArgumentException("환전 금액은 양수여야 합니다")
        }
        
        // 최소 환전 금액 확인 (예: 1000 크레딧 이상)
        val minExchangeAmount = 1000
        if (request.amount < minExchangeAmount) {
            throw IllegalArgumentException("최소 환전 금액은 $minExchangeAmount 크레딧입니다")
        }
        
        val user = findUser(request.userId)
        val credit = findOrCreateUserCredit(user.id)
        val balanceBefore = credit.balance
        
        // 잔액 확인
        if (balanceBefore < request.amount) {
            logger.warn("크레딧 부족: userId={}, required={}, balance={}", 
                user.id, request.amount, balanceBefore)
            
            // 실패 트랜잭션 기록
            val failedTransaction = CreditTransaction(
                user = user,
                amount = request.amount,
                type = TransactionType.EXCHANGE,
                description = "크레딧 환전 시도 (${request.bankName ?: ""}, ${request.accountNumber ?: ""})",
                status = TransactionStatus.FAILED,
                balanceBefore = balanceBefore,
                balanceAfter = balanceBefore // 잔액 변동 없음
            )
            // 별도 트랜잭션으로 저장하여 롤백 방지
            creditTransactionRepository.saveAndFlush(failedTransaction)
            
            // 결과 DTO 반환 (트랜잭션 롤백되지 않음)
            return TransactionResultDto(
                transactionId = failedTransaction.id,
                status = TransactionStatus.FAILED.name,
                newBalance = balanceBefore,
                message = "크레딧이 부족합니다 (필요: ${request.amount}, 보유: $balanceBefore)"
            )
        }
        
        // 트랜잭션 기록 생성
        val transaction = CreditTransaction(
            user = user,
            amount = request.amount,
            type = TransactionType.EXCHANGE,
            description = "크레딧 환전 (${request.bankName ?: ""}, ${request.accountNumber ?: ""}, ${request.accountHolder ?: ""})",
            status = TransactionStatus.PENDING,
            balanceBefore = balanceBefore
        )
        creditTransactionRepository.save(transaction)
        
        try {
            // 크레딧 잔액 업데이트
            val time = OffsetDateTime.now()

            credit.balance -= request.amount
            credit.updatedAt = time
            creditRepository.save(credit)
            
            // 실제 환전 처리 로직은 여기에 구현
            // 외부 결제 시스템이나 관리자 승인 프로세스 연동 등
            
            // 트랜잭션 상태 업데이트
            transaction.status = TransactionStatus.COMPLETED
            transaction.balanceAfter = credit.balance
            transaction.updatedAt = time
            creditTransactionRepository.save(transaction)
            
            logger.info("크레딧 환전 요청 성공: userId={}, amount={}, newBalance={}, bank={}, account={}", 
                user.id, request.amount, credit.balance, request.bankName, request.accountNumber)
            
            return TransactionResultDto(
                transactionId = transaction.id,
                status = TransactionStatus.COMPLETED.name,
                newBalance = credit.balance,
                message = "크레딧 환전 요청이 완료되었습니다. 처리 후 지정된 계좌로 입금됩니다."
            )
        } catch (e: Exception) {
            // 오류 발생 시 트랜잭션 상태 업데이트
            transaction.status = TransactionStatus.FAILED
            transaction.updatedAt = OffsetDateTime.now()
            creditTransactionRepository.save(transaction)
            
            logger.error("크레딧 환전 실패: userId={}, amount={}, error={}", 
                user.id, request.amount, e.message, e)
            
            throw RuntimeException("크레딧 환전 중 오류가 발생했습니다: ${e.message}", e)
        }
    }
} 