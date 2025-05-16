package kr.ac.kookmin.cs.capstone.voicepack_platform.sale.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.dto.SaleDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.dto.SalesSummaryDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.service.SalesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sales")
@Tag(name = "판매 관리", description = "보이스팩 판매 내역 및 통계 관련 API")
class SalesController(
    private val salesService: SalesService
) {

    @Operation(
        summary = "판매 내역 조회",
        description = "사용자의 보이스팩 판매 내역을 페이지네이션하여 조회합니다.",
    )
    @GetMapping("") // API 명세상 /api/sales 와 /api/sales/list 가 유사하여 통합
    fun getSalesHistory(
        @Parameter(description = "판매자(로그인 사용자) ID") @RequestParam sellerId: Long, // TODO: 인증 정보 사용
        @Parameter(required = false, description = "페이지 정보", example = """
            {
                "page": 0,
                "size": 10,
                "sort": [
                    "transactionDate"
                ]
            }
        """
        ) @PageableDefault(size = 10, sort = ["transactionDate"]) pageable: Pageable // 기본 10개, 날짜 정렬
    ): ResponseEntity<Page<SaleDto>> {
        val salesHistory = salesService.getSalesHistory(sellerId, pageable)
        return ResponseEntity.ok(salesHistory)
    }

    @Operation(
        summary = "판매 통계 조회",
        description = "사용자의 총 수익, 이번 달 수익, 총 판매 건수 통계를 조회합니다."
    )
    @GetMapping("/summary")
    fun getSalesSummary(
        @Parameter(description = "판매자(로그인 사용자) ID") @RequestParam sellerId: Long // TODO: 인증 정보 사용
    ): ResponseEntity<SalesSummaryDto> {
        val summary = salesService.getSalesSummary(sellerId)
        return ResponseEntity.ok(summary)
    }
} 