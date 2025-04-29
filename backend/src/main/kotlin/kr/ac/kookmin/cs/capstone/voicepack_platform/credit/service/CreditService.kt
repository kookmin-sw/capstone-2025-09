package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.service

import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.Credit
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.CreditTransaction
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.ReferenceType
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionType
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.CreditExchangeRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.ExchangeStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository.CreditRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository.CreditTransactionRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository.CreditExchangeRequestRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.SaleRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class CreditService(
    private val creditRepository: CreditRepository,
    private val creditTransactionRepository: CreditTransactionRepository,
    private val creditExchangeRequestRepository: CreditExchangeRequestRepository,
    private val saleRepository: SaleRepository,
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
            description = request.paymentMethod ?: "크레딧 충전: 일반",
            status = TransactionStatus.PENDING,
            balanceBefore = balanceBefore,
            referenceId = request.paymentReference?.toLong(),
            referenceType = if (request.paymentMethod?.contains("보이스팩") == true) ReferenceType.VOICEPACK else ReferenceType.PAYMENT
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
    fun useCredits(userId: Long, request: UseCreditsRequest): TransactionResultDto {
        logger.info("크레딧 사용 요청: {}", request)
        
        if (request.amount <= 0) {
            throw IllegalArgumentException("사용 금액은 양수여야 합니다")
        }
        
        val user = findUser(userId)
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
     * TODO: Page<CreditTransactionDto> 대신 CreditHistoryDto 반환하도록 수정 필요
     */
    @Transactional(readOnly = true)
    fun getTransactionHistory(userId: Long): CreditHistoryDto {
        logger.info("크레딧 거래 내역 조회: userId={}", userId)
        
        // 사용자의 모든 완료된 거래 내역 조회 (최신순)
        // TODO: 페이징 처리 필요 시 추가 고려
        val transactions = creditTransactionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, TransactionStatus.COMPLETED)
        
        val charges = transactions.filter { it.type == TransactionType.CHARGE || it.type == TransactionType.SALE_INCOME }.map { 
            // TODO: ChargeDto의 amountWon, method 필드 채우는 로직 필요 (description 파싱 등)
            ChargeDto(
                date = it.createdAt,
                amountWon = null, // 임시
                amountCredit = it.amount,
                method = it.description // 임시
            )
        }
        
        val usages = transactions.filter { it.type == TransactionType.PURCHASE || it.type == TransactionType.EXCHANGE }.map { 
            UsageDto(
                date = it.createdAt,
                usage = it.description,
                amountCredit = it.amount
            )
        }
        
        return CreditHistoryDto(charges = charges, usages = usages)
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
    
    // 사용자 총 수입 조회 (판매 수익 합계)
    @Transactional(readOnly = true)
    fun getTotalEarnings(userId: Long): Int {
        logger.info("총 수입 조회: userId={}", userId)
        return saleRepository.sumAmountBySellerId(userId) ?: 0 
        
    }

    // API 요구사항 9번: 크레딧 환전 신청 요청
    @Transactional
    fun requestCreditExchange(userId: Long, request: CreditExchangeRequestRequestDto): CreditExchangeResponseDto {
        logger.info("크레딧 환전 신청 요청: userId={}, requestCredit={}", userId, request.credit)

        if (request.credit <= 0) {
            throw IllegalArgumentException("환전 신청 크레딧은 양수여야 합니다")
        }
        
        // TODO: 환전 비율 적용하여 원화 계산 (예: 1 크레딧 = 100원)
        val exchangeRate = 100
        val wonAmount = request.credit * exchangeRate
        
        val user = findUser(userId)
        val credit = findOrCreateUserCredit(user.id)
        val balanceBefore = credit.balance
        
        // 잔액 확인
        if (balanceBefore < request.credit) {
            logger.warn("크레딧 부족 (환전): userId={}, required={}, balance={}", 
                user.id, request.credit, balanceBefore)
            // 실패 시에는 별도 트랜잭션 기록 없이 예외 발생 또는 실패 응답 반환
            throw IllegalStateException("환전 신청 실패: 크레딧이 부족합니다 (필요: ${request.credit}, 보유: $balanceBefore)")
        }
        
        // CreditExchangeRequest 생성 및 저장
        val exchangeRequest = CreditExchangeRequest(
            user = user,
            creditAmount = request.credit,
            wonAmount = wonAmount,
            status = ExchangeStatus.PENDING, // 초기 상태는 PENDING
            // TODO: request에서 bankName, accountNumber, accountHolder 정보 받아와서 저장
            bankName = request.bankName,
            accountNumber = request.accountNumber,
            accountHolder = request.accountHolder
        )
        val savedExchangeRequest = creditExchangeRequestRepository.save(exchangeRequest) // 저장 후 ID 사용
        logger.info("환전 신청 기록 저장: userId={}, credit={}, won={}, status={}, requestId={}", 
            userId, request.credit, wonAmount, exchangeRequest.status, savedExchangeRequest.id)
        
        // 크레딧 차감 (환전 신청 시점에 즉시 차감)
        val useCreditResult = useCredits(userId, UseCreditsRequest(
            amount = request.credit,
            referenceId = savedExchangeRequest.id, // 생성된 환전 요청 ID 참조
            referenceType = ReferenceType.CREDIT_EXCHANGE,
            description = "크레딧 환전 신청"
        ))
        
        if (useCreditResult.status == TransactionStatus.FAILED.name) {
            // 이 경우는 잔액 부족 외 다른 이유로 차감 실패 시
            logger.error("환전 신청 실패: 크레딧 차감 중 오류 발생 - userId={}, credit={}", userId, request.credit)
            // TODO: 생성된 exchangeRequest 상태를 REJECTED로 변경하거나 삭제하는 로직 추가 고려
            throw RuntimeException("환전 신청 처리 중 오류가 발생했습니다: ${useCreditResult.message}")
        }
        
        // 성공 응답 반환
        return CreditExchangeResponseDto(
            message = "환전 신청이 완료되었습니다. 처리 후 입금됩니다.",
            won = wonAmount
        )
    }
    
    // API 요구사항 8번: 크레딧 환전 신청 내역 조회
    @Transactional(readOnly = true)
    fun getCreditExchangeRequests(userId: Long, pageable: Pageable): Page<CreditExchangeRequestDto> {
        logger.info("환전 신청 내역 조회: userId={}, page={}, size={}", userId, pageable.pageNumber, pageable.pageSize)
        val requestsPage = creditExchangeRequestRepository.findByUserIdOrderByRequestDateDesc(userId, pageable)
        return requestsPage.map { CreditExchangeRequestDto.fromEntity(it) }
    }
    
    // API 요구사항 10번: 크레딧 충전 요청
    // TODO: 실제 결제 시스템 연동 필요
    fun requestCreditCharge(userId: Long, request: CreditChargeRequestDto): Map<String, Any> {
        logger.info("크레딧 충전 요청 시작: userId={}, amount={}, method={}", userId, request.amount, request.method)
        
        if (request.amount <= 0) {
            throw IllegalArgumentException("충전 금액은 양수여야 합니다.")
        }
        val user = findUser(userId) // 사용자가 존재하는지 확인
        
        // TODO: 결제 정보 생성 (주문 ID 등)
        val orderId = "temp_order_${System.currentTimeMillis()}" 
        
        // TODO: 결제 모듈 호출 및 결제 페이지 URL 또는 정보 반환
        // 현재는 임시로 성공 메시지와 주문 ID 반환
        val response = mapOf(
            "message" to "결제 요청이 생성되었습니다. 결제를 진행해주세요.",
            "orderId" to orderId,
            "amount" to request.amount,
            "paymentMethod" to request.method,
            "nextRedirectUrl" to "/payment/process?orderId=$orderId" // 예시 URL
        )
        
        logger.info("크레딧 충전 요청 생성 완료: userId={}, orderId={}", userId, orderId)
        return response
    }
} 