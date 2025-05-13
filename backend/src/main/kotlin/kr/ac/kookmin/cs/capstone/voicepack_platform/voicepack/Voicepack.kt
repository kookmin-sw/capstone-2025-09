package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRight
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.VoiceSynthesisRequest
import java.time.OffsetDateTime
import io.swagger.v3.oas.annotations.media.Schema

@Entity
@Table(name = "voicepack")
data class Voicepack(
    @Id
    @Column(name = "voicepack_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name", nullable = false, unique = true)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    @Column(name = "s3_path", nullable = false)
    val s3Path: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "price")
    var price: Int? = 1000,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,

    @Column(name = "image_s3_key", nullable = true)
    var imageS3Key: String? = null,

    @Column(name = "categories_json", length = 1024, nullable = false)
    var categoriesJson: String,

    @OneToMany(mappedBy = "voicepack", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val usageRights: MutableList<VoicepackUsageRight> = mutableListOf(),

    @OneToMany(mappedBy = "voicepack", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val synthesisRequests: MutableList<VoiceSynthesisRequest> = mutableListOf()
) 

/**
 * 보이스팩 DTO 클래스
 * s3Path를 제외한 보이스팩 정보를 담는 데이터 클래스입니다.
 */
data class VoicepackDto(
    @Schema(description = "보이스팩 ID")
    val id: Long,
    @Schema(description = "보이스팩 이름")
    val name: String,
    @Schema(description = "제작자")
    val author: String,
    @Schema(description = "생성 일시")
    val createdAt: OffsetDateTime,
    @Schema(description = "가격 (크레딧)")
    val price: Int?,
    @Schema(description = "공개 여부")
    val isPublic: Boolean,
    @Schema(description = "대표 이미지 URL (Presigned URL)", nullable = true)
    val imageUrl: String?,
    @Schema(description = "카테고리 목록", nullable = false)
    val categories: List<String>
) {
    companion object {
        fun fromEntity(
            voicepack: Voicepack,
            presignedImageUrl: String?,
            parsedCategories: List<String>
        ): VoicepackDto {
            return VoicepackDto(
                id = voicepack.id,
                name = voicepack.name,
                author = voicepack.author.email,
                createdAt = voicepack.createdAt,
                price = voicepack.price,
                isPublic = voicepack.isPublic,
                imageUrl = presignedImageUrl,
                categories = parsedCategories
            )
        }
    }
}
