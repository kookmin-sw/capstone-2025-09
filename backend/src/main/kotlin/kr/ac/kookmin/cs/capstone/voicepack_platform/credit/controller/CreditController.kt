package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.service.CreditService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/credits")
@Tag(name = "크레딧", description = "사용자 크레딧 관리 API")
class CreditController(
    private val creditService: CreditService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    @Operation(
        summary = "크레딧 잔액 조회",
        description = "사용자의 현재 크레딧 잔액을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공",
                content = [Content(schema = Schema(implementation = CreditBalanceDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            )
        ]
    )
    @GetMapping("/balance/{userId}")
    fun getBalance(
        @Parameter(description = "사용자 ID") 
        @PathVariable userId: Long
    ): ResponseEntity<CreditBalanceDto> {
        try {
            val balance = creditService.getUserBalance(userId)
            return ResponseEntity.ok(balance)
        } catch (e: IllegalArgumentException) {
            logger.error("잔액 조회 실패: {}", e.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: Exception) {
            logger.error("잔액 조회 중 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @Operation(
        summary = "크레딧 충전",
        description = "사용자의 크레딧을 충전합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "충전 성공",
                content = [Content(schema = Schema(implementation = TransactionResultDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @PostMapping("/charge")
    fun chargeCredits(
        @RequestBody request: ChargeCreditsRequest
    ): ResponseEntity<TransactionResultDto> {
        try {
            val result = creditService.chargeCredits(request)
            return ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            logger.error("충전 요청 실패: {}", e.message)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("충전 중 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @Operation(
        summary = "크레딧 사용",
        description = "특정 목적으로 사용자의 크레딧을 사용합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용 성공",
                content = [Content(schema = Schema(implementation = TransactionResultDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "402",
                description = "크레딧 부족"
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @PostMapping("/use")
    fun useCredits(
        @RequestBody request: UseCreditsRequest
    ): ResponseEntity<TransactionResultDto> {
        try {
            val result = creditService.useCredits(request)
            return ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            logger.error("사용 요청 실패: {}", e.message)
            return ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            // 크레딧 부족
            logger.error("크레딧 부족: {}", e.message)
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build()
        } catch (e: Exception) {
            logger.error("크레딧 사용 중 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @Operation(
        summary = "크레딧 환전",
        description = "사용자의 크레딧을 현금으로 환전합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "환전 요청 성공",
                content = [Content(schema = Schema(implementation = TransactionResultDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "402",
                description = "크레딧 부족"
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @PostMapping("/exchange")
    fun exchangeCredits(
        @RequestBody request: ExchangeCreditsRequest
    ): ResponseEntity<TransactionResultDto> {
        try {
            val result = creditService.exchangeCredits(request)
            return ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            logger.error("환전 요청 실패: {}", e.message)
            return ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            // 크레딧 부족
            logger.error("크레딧 부족: {}", e.message)
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build()
        } catch (e: Exception) {
            logger.error("크레딧 환전 중 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @Operation(
        summary = "거래 내역 조회",
        description = "사용자의 크레딧 거래 내역을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/transactions")
    fun getTransactions(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "거래 유형 (CHARGE, PURCHASE, REFUND, SYSTEM)") @RequestParam(required = false) type: String?,
        @Parameter(description = "거래 상태 (PENDING, COMPLETED, FAILED)") @RequestParam(required = false) status: String?,
        @Parameter(description = "시작 날짜 (ISO-8601 형식)") @RequestParam(required = false) startDate: String?,
        @Parameter(description = "종료 날짜 (ISO-8601 형식)") @RequestParam(required = false) endDate: String?,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<CreditTransactionDto>> {
        try {
            val request = GetTransactionsRequest(
                userId = userId,
                type = type,
                status = status,
                startDate = startDate,
                endDate = endDate,
                page = page,
                size = size
            )
            
            val transactions = creditService.getTransactionHistory(request)
            return ResponseEntity.ok(transactions)
        } catch (e: IllegalArgumentException) {
            logger.error("거래 내역 조회 실패: {}", e.message)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("거래 내역 조회 중 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
} 