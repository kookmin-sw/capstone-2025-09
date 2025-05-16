package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.service.CreditService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
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
        deprecated = true,
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
    @PostMapping("/use/{userId}")
    fun useCredits(
        @Parameter(description = "사용자 ID") @PathVariable userId: Long,
        @RequestBody request: UseCreditsRequest
    ): ResponseEntity<Any> {
        try {
            val result = creditService.useCredits(userId, request)
            
            // 상태에 따라 다른 HTTP 응답 코드 반환
            return when (result.status) {
                TransactionStatus.COMPLETED.name -> ResponseEntity.ok(result)
                TransactionStatus.FAILED.name -> {
                    if (result.message?.contains("크레딧이 부족") == true) {
                        ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(result)
                    } else {
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result)
                    }
                }
                else -> ResponseEntity.status(HttpStatus.PROCESSING).body(result)
            }
        } catch (e: IllegalArgumentException) {
            logger.error("사용 요청 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("크레딧 사용 중 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "크레딧 사용 중 오류가 발생했습니다"))
        }
    }
    
    @Operation(
        deprecated = true,
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
        @RequestBody request: CreditExchangeRequestRequestDto
    ): ResponseEntity<Any> {
        try {
            val result = creditService.requestCreditExchange(1L, request)
            
            // 성공 응답 처리 (CreditExchangeResponseDto 반환)
            return ResponseEntity.ok(result)

        } catch (e: IllegalStateException) {
            logger.warn("환전 신청 실패: {}", e.message)
            // 크레딧 부족 또는 기타 비즈니스 로직 예외
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.error("환전 요청 실패 (잘못된 인자): {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: RuntimeException) {
            logger.error("환전 처리 중 내부 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "환전 처리 중 오류가 발생했습니다"))
        } catch (e: Exception) {
            logger.error("알 수 없는 환전 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "알 수 없는 오류가 발생했습니다"))
        }
    }
    
    @Operation(
        summary = "크레딧 거래 내역 조회 (충전/사용)",
        description = "사용자의 크레딧 충전 및 사용 내역을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = CreditHistoryDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @GetMapping("/history/{userId}")
    fun getCreditHistory(
        @Parameter(description = "사용자 ID") @PathVariable userId: Long
    ): ResponseEntity<CreditHistoryDto> {
        return try {
            val history = creditService.getTransactionHistory(userId)
            ResponseEntity.ok(history)
        } catch (e: NoSuchElementException) {
            logger.warn("크레딧 내역 조회 실패: 사용자를 찾을 수 없음 - userId={}", userId)
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: Exception) {
            logger.error("크레딧 내역 조회 중 오류 발생: userId={}, error={}", userId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // API 요구사항 8번: 환전 신청 내역 조회
    @Operation(
        summary = "크레딧 환전 신청 내역 조회",
        description = "사용자의 크레딧 환전 신청 내역 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @GetMapping("/exchange-request/{userId}")
    fun getCreditExchangeRequests(
        @Parameter(description = "사용자 ID") @PathVariable userId: Long,
        @Parameter(required = false, description = "페이지 정보", example = """
        {
            "page": 0,
            "size": 10,
            "sort": [
                "requestDate"
            ]
        }
    """
    ) @PageableDefault(size = 10, sort = ["requestDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<CreditExchangeRequestDto>> {
        return try {
            val history = creditService.getCreditExchangeRequests(userId, pageable)
            ResponseEntity.ok(history)
        } catch (e: Exception) {
            logger.error("환전 신청 내역 조회 중 오류 발생: userId={}, error={}", userId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // API 요구사항 9번: 크레딧 환전 신청 요청
    @Operation(
        summary = "크레딧 환전 신청",
        description = "사용자의 크레딧을 현금으로 환전 신청합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "신청 성공", content = [Content(schema = Schema(implementation = CreditExchangeResponseDto::class))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (금액 오류 등)"),
            ApiResponse(responseCode = "402", description = "크레딧 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @PostMapping("/exchange-request/{userId}")
    fun requestCreditExchange(
        @Parameter(description = "사용자 ID") @PathVariable userId: Long,
        @RequestBody request: CreditExchangeRequestRequestDto
    ): ResponseEntity<Any> {
        return try {
            val response = creditService.requestCreditExchange(userId, request)
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            logger.warn("환전 신청 실패: userId={}, error={}", userId, e.message)
            ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.warn("환전 신청 실패 (잘못된 요청): userId={}, error={}", userId, e.message)
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NoSuchElementException) {
            logger.warn("환전 신청 실패: 사용자를 찾을 수 없음 - userId={}", userId)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("환전 신청 처리 중 오류 발생: userId={}, error={}", userId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "환전 신청 처리 중 오류가 발생했습니다."))
        }
    }
    
    // API 요구사항 10번: 크레딧 충전 요청
    @Operation(
        summary = "크레딧 충전 요청 생성",
        description = "크레딧 충전을 위한 결제 요청을 생성합니다. (실제 결제 처리는 별도)",
        responses = [
            ApiResponse(responseCode = "200", description = "결제 요청 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @PostMapping("/charge/{userId}")
    fun requestCreditCharge(
        @Parameter(description = "사용자 ID") @PathVariable userId: Long,
        @RequestBody request: CreditChargeRequestDto
    ): ResponseEntity<Any> {
        return try {
            val response = creditService.requestCreditCharge(userId, request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("충전 요청 생성 실패 (잘못된 요청): userId={}, error={}", userId, e.message)
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NoSuchElementException) {
            logger.warn("충전 요청 생성 실패: 사용자를 찾을 수 없음 - userId={}", userId)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("충전 요청 생성 중 오류 발생: userId={}, error={}", userId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "충전 요청 처리 중 오류가 발생했습니다."))
        }
    }
} 