package kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto

import io.swagger.v3.oas.annotations.media.Schema

data class UserProfileDto(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long,
    
    @Schema(description = "사용자 이름", example = "박수연")
    val name: String?,
    
    @Schema(description = "사용자 이메일", example = "suwith@kookmin.ac.kr")
    val email: String,
    
    @Schema(description = "프로필 이미지 URL", example = "https://...")
    val profileImageUrl: String?,
    
    @Schema(description = "보유 크레딧", example = "320")
    val credit: Int,
    
    @Schema(description = "총 수입 (판매로 얻은 크레딧 합계)", example = "1200") // API 명세의 120000은 오타로 가정
    val totalEarnings: Int, 
    
    @Schema(description = "생성한 보이스팩 수", example = "5")
    val createdPacks: Int,
    
    @Schema(description = "구매한 보이스팩 수", example = "7")
    val boughtPacks: Int
) 