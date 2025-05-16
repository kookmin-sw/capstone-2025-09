package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreditExchangeRequestRequestDto(
    @Schema(description = "환전 신청할 크레딧 양", example = "320")
    val credit: Int,

    // TODO: 환전 계좌 정보 필드 추가 필요 (bankName, accountNumber, accountHolder)
    @Schema(description = "은행 이름", example = "국민은행")
    val bankName: String,
    @Schema(description = "계좌 번호", example = "123-456-789")
    val accountNumber: String,
    @Schema(description = "예금주명", example = "홍길동")
    val accountHolder: String
) 