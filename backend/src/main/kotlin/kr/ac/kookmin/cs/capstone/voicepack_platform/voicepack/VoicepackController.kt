package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisStatusDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightBriefDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/voicepack")
@Tag(name = "보이스팩", description = "보이스팩 관련 API")
class VoicepackController(
    private val voicepackService: VoicepackService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(
        summary = "보이스팩 변환",
        description = "사용자가 업로드한 음성 파일을 보이스팩으로 변환합니다.",
        responses = [
            ApiResponse(
                responseCode = "202",
                description = "변환 요청 성공",
                content = [Content(schema = Schema(implementation = VoicepackConvertResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (예: 사용자 ID 누락, 필수 파라미터 누락, 이름 중복)"
            ),
            ApiResponse(
                responseCode = "409",
                description = "충돌 발생 (예: 이미 진행 중인 변환 요청)"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @PostMapping("/convert", consumes = ["multipart/form-data"])
    suspend fun convertVoicepack(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "보이스팩 이름") @RequestParam("name") name: String,
        @Parameter(description = "음성 파일") @RequestPart("voiceFile") voiceFile: MultipartFile,
        @Parameter(description = "보이스팩 대표 이미지 파일 (선택 사항)") @RequestPart("imageFile", required = false) imageFile: MultipartFile?,
        @Parameter(description = "카테고리 목록 (필수 항목)") @RequestParam("categories") categories: List<String>
    ): ResponseEntity<Any> {
        return try {
            val request = VoicepackConvertRequest(name, voiceFile, imageFile, categories)
            val response = voicepackService.convertVoicepack(userId, request)
            ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("보이스팩 변환 요청 유효성 검사 실패: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: IllegalStateException) {
            logger.warn("보이스팩 변환 요청 충돌: {}", e.message)
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("보이스팩 변환 중 예상치 못한 오류 발생: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "보이스팩 변환 중 오류가 발생했습니다."))
        }
    }

    @Operation(
        summary = "보이스팩 기반 TTS 생성 요청 (비동기)",
        description = "사용자가 보이스팩을 기반으로 TTS 생성을 비동기적으로 요청합니다.",
        responses = [
            ApiResponse(
                responseCode = "202",
                description = "생성 요청 성공 (처리 시작됨)",
                content = [Content(schema = Schema(implementation = VoicepackSynthesisSubmitResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "403",
                description = "사용 권한 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류 (Lambda 호출 실패 등)"
            )
        ]
    )
    @PostMapping("/synthesis")
    suspend fun submitSynthesisRequest(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @RequestBody request: VoicepackSynthesisRequest
    ): ResponseEntity<Any> {
        try {
            val response = voicepackService.submitSynthesisRequest(userId, request)
            
            // Location 헤더 생성 (상태 조회 엔드포인트 URL)
            val locationUri = ServletUriComponentsBuilder
                .fromCurrentContextPath() // 현재 요청의 기본 URL (e.g., http://localhost:8080)
                .path("/api/voicepack/synthesis/status/{id}") // 상태 조회 경로 추가
                .buildAndExpand(response.id) // 경로 변수({id}) 채우기
                .toUri()
                
            return ResponseEntity.accepted().location(locationUri).body(response)
        } catch (e: SecurityException) {
            logger.error("음성 합성 권한 오류: {}", e.message)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.error("음성 합성 잘못된 요청: {}", e.message)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: RuntimeException) {
            logger.error("음성 합성 요청 제출 오류: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("음성 합성 중 예상치 못한 오류: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "음성 합성 요청 중 오류가 발생했습니다."))
        }
    }

    @Operation(
        summary = "보이스팩 목록 조회",
        description = "시스템에 등록된 모든 보이스팩을 조회합니다. 선택적으로 특정 사용자의 보이스팩만 조회할 수 있습니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = List::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("")
    fun getVoicepacks(
        @Parameter(description = "사용자 ID ('mine', 'purchased', 'available' 필터 사용 시 필요)") @RequestParam(required = false) userId: Long?,
        @Parameter(description = "필터 (all | mine | purchased | available), 기본값: all(공개된 보이스팩)") @RequestParam(required = false, defaultValue = "all") filter: String?
    ): ResponseEntity<List<VoicepackDto>> {
        try {
            val voicepacks = voicepackService.getVoicepacks(userId, filter)
            return ResponseEntity.ok(voicepacks)
        } catch (e: IllegalArgumentException) {
            // userId 누락 등 서비스 레벨에서 발생한 오류 처리
            return ResponseEntity.badRequest().body(listOf()) // 혹은 에러 메시지 반환
        } catch (e: Exception) {
            logger.error("보이스팩 목록 조회 중 오류 발생: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf())
        }
    }
  
    // 보이스팩 1개만 조회
    @Operation(
        summary = "보이스팩 1개 조회",
        description = "보이스팩 1개를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",   
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = VoicepackDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "보이스팩 존재하지 않음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )

    @GetMapping("/{voicepackId}")
    fun getVoicepack(
        @Parameter(description = "보이스팩 ID") @PathVariable voicepackId: Long
    ): ResponseEntity<Any> {
        return try {
            val response = voicepackService.getVoicepack(voicepackId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("보이스팩 조회 실패: voicepackId={}, error={}", voicepackId, e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("보이스팩 조회 중 예상치 못한 오류 발생: voicepackId={}, error={}", voicepackId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "보이스팩 조회 중 오류가 발생했습니다."))
        }
    }

    @Operation(
        summary = "보이스팩 삭제",
        description = "보이스팩을 삭제합니다.",
        responses = [
            ApiResponse(responseCode = "204", description = "삭제 성공 (콘텐츠 없음)")
        ]
    )
    @DeleteMapping("/{voicepackId}")
    fun deleteVoicepack(
        @Parameter(description = "삭제 요청하는 사용자 ID (로그인 필요)") @RequestParam userId: Long, // TODO: 추후 SecurityContextHolder에서 가져오도록 변경
        @Parameter(description = "보이스팩 ID") @PathVariable voicepackId: Long
    ): ResponseEntity<Any> {
        return try {
            voicepackService.deleteVoicepack(userId, voicepackId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("보이스팩 삭제 실패 (찾을 수 없음): voicepackId={}, error={}", voicepackId, e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: SecurityException) {
            logger.warn("보이스팩 삭제 권한 없음: userId={}, voicepackId={}, error={}", userId, voicepackId, e.message)
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("보이스팩 삭제 중 오류 발생: voicepackId={}, error={}", voicepackId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "서버 처리 중 오류가 발생했습니다."))
        }
    }
    
    // 보이스팩 예시 음성 파일 조회
    @Operation(
        summary = "보이스팩 예시 음성 파일 조회",
        description = "보이스팩 예시 음성 파일을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "보이스팩 존재하지 않음"
            ),
            ApiResponse(
                responseCode = "500",   
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/example/{voicepackId}")
    fun getExampleVoicepack(
        @Parameter(description = "보이스팩 ID") @PathVariable voicepackId: Long
    ): ResponseEntity<Any> {
        return try {
            val response = voicepackService.getExampleVoice(voicepackId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("보이스팩 예시 음성 조회 실패: voicepackId={}, error={}", voicepackId, e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("보이스팩 예시 음성 조회 중 예상치 못한 오류 발생: voicepackId={}, error={}", voicepackId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "예시 음성 조회 중 오류가 발생했습니다."))
        }
    }

    @Operation(
        summary = "보이스팩 사용권 획득",
        description = "사용자가 특정 보이스팩의 사용권을 획득합니다 (구매 또는 제작자 자동 획득).",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용권 획득 성공",
                content = [Content(schema = Schema(implementation = VoicepackUsageRightDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (예: 사용자 또는 보이스팩 없음)"
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 사용권을 가지고 있는 보이스팩"
            ),
            ApiResponse(
                responseCode = "402",
                description = "크레딧 부족 (크레딧 연동 시)"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @PostMapping("/usage-right")
    fun grantUsageRight(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "보이스팩 ID") @RequestParam voicepackId: Long
    ): ResponseEntity<Any> {
        try {
            val usageRightDto = voicepackService.grantUsageRight(userId, voicepackId)
            return ResponseEntity.ok(usageRightDto)
        } catch (e: IllegalArgumentException) {
            logger.warn("보이스팩 사용권 획득 잘못된 요청: userId={}, voicepackId={}, error={}", userId, voicepackId, e.message)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: IllegalStateException) {
            logger.warn("보이스팩 사용권 획득 충돌 또는 비즈니스 로직 오류: userId={}, voicepackId={}, error={}", userId, voicepackId, e.message)
            if (e.message?.contains("크레딧") == true || e.message?.contains("Credit") == true ) {
                 return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(mapOf("error" to e.message))
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))
        } catch (e: RuntimeException) {
             logger.error("보이스팩 사용권 획득 중 런타임 오류: userId={}, voicepackId={}, error={}", userId, voicepackId, e.message, e)
            if (e.message?.contains("크레딧") == true || e.message?.contains("Credit") == true) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(mapOf("error" to e.message))
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "사용권 획득 처리 중 오류가 발생했습니다.")))
        } catch (e: Exception) {
            logger.error("보이스팩 사용권 획득 중 예상치 못한 오류: userId={}, voicepackId={}, error={}", userId, voicepackId, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "사용권 획득 처리 중 예상치 못한 오류가 발생했습니다."))
        }
    }


    // 사용자가 사용권을 보유한 보이스팩 목록 조회
    @Operation(
        summary = "사용자가 사용권을 보유한 보이스팩 목록 조회",
        description = "사용자가 사용권을 보유한 보이스팩 목록을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = List::class))]
            ),
            ApiResponse(
                responseCode = "500",   
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/usage-right")
    fun getUserVoicepacks(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long
    ): ResponseEntity<List<VoicepackUsageRightBriefDto>> {
        val voicepacks = voicepackService.getVoicepacksByUserId(userId)
        return ResponseEntity.ok(voicepacks)
    }

    // 음성 합성 시 모델로부터 부를 callback url
    @Operation(
        summary = "음성 합성 콜백",
        description = "음성 합성 처리 완료 후 Cloud Run에서 호출하는 내부 콜백 엔드포인트입니다.",
        deprecated = true
    )
    @PostMapping("/synthesis/callback")
    fun handleSynthesisCallback(
        @Parameter(description = "요청 ID") @RequestParam id: Long,
        @Parameter(description = "처리 성공 여부") @RequestParam success: Boolean,
        @Parameter(description = "성공 시 결과 URL") @RequestParam(required = false) resultUrl: String?,
        @Parameter(description = "실패 시 오류 메시지") @RequestParam(required = false) errorMessage: String?
    ): ResponseEntity<Any> {
        // RequestParam으로 받은 값들을 사용하여 DTO 객체 생성
        val callbackRequest = VoicepackCallbackRequest(
            id = id,
            success = success,
            resultUrl = resultUrl,
            errorMessage = errorMessage
        )
        
        try {
            voicepackService.handleSynthesisCallback(callbackRequest)
            return ResponseEntity.ok().build() // 성공 시 200 OK만 반환
        } catch (e: Exception) {
            // 콜백 처리 중 오류 발생 시 로깅만 하고 500 에러 반환 (Cloud Run 재시도 방지 목적)
            logger.error("음성 합성 콜백 처리 중 오류 발생: id={}, error={}", callbackRequest.id, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "콜백 처리 중 오류 발생"))
        }
    }

    @Operation(
        summary = "보이스팩 변환 상태 조회 (Polling)",
        description = "보이스팩 변환 요청의 현재 상태와 결과(완료 시)를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = VoicepackSynthesisStatusDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 Job ID의 요청을 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/convert/status/{id}")
    fun getConvertStatus(
        @Parameter(description = "조회할 요청의 ID") @PathVariable id: Long
    ): ResponseEntity<Any> = runCatching {
        voicepackService.getConvertStatus(id)
    }.fold(
        onSuccess = { ResponseEntity.ok(it) }, // it은 성공 시 리턴값
        onFailure = { e ->
            when (e) {
                is IllegalArgumentException -> {
                    logger.warn("보이스팩 변환 상태 조회 실패: {}", e.message)
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
                }
                else -> {
                    logger.error("보이스팩 변환 상태 조회 중 오류 발생: id={}, error={}", id, e.message, e)
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf("error" to "상태 조회 중 오류 발생"))
                }
            }
        }
    )

    @Operation(
        summary = "음성 합성 상태 조회 (Polling)",
        description = "제출된 음성 합성 요청의 현재 상태와 결과(완료 시)를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = VoicepackSynthesisStatusDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 Job ID의 요청을 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/synthesis/status/{id}")
    fun getSynthesisStatus(
        @Parameter(description = "조회할 요청의 ID") @PathVariable id: Long
    ): ResponseEntity<Any> {
        try {
            val statusDto = voicepackService.getSynthesisStatus(id)
            return ResponseEntity.ok(statusDto)
        } catch (e: IllegalArgumentException) {
            logger.warn("음성 합성 상태 조회 실패: {}", e.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("음성 합성 상태 조회 중 오류 발생: id={}, error={}", id, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "상태 조회 중 오류 발생"))
        }
    }

    // 디버그용 엔드포인트
    @Operation(
        summary = "디버그용 엔드포인트",
        description = "디버그용 엔드포인트입니다.",
        deprecated = true
    )
    @PostMapping("/debug/create-voicepack")
    fun createVoicepackForDebug(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "보이스팩 ID") @RequestParam voicepackId: Long
    ): ResponseEntity<Any> {
        voicepackService.createVoicepackForDebug(userId, voicepackId)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "보이스팩 공개 여부 변경",
        description = "특정 보이스팩의 공개 여부를 변경합니다. 작성자 본인만 요청 가능합니다.",
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "변경 성공", 
                content = [Content(schema = Schema(implementation = VoicepackDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "404", description = "보이스팩을 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @PatchMapping("/{voicepackId}")
    fun updateVoicepackPublicStatus(
        @Parameter(description = "수정 요청하는 사용자 ID (로그인 필요)") @RequestParam userId: Long, // TODO: 추후 SecurityContextHolder에서 가져오도록 변경
        @Parameter(description = "수정할 보이스팩 ID") @PathVariable voicepackId: Long,
        @RequestBody request: UpdateVoicepackPublicRequest
    ): ResponseEntity<Any> {
        return try {
            val updatedVoicepackDto = voicepackService.updateVoicepackPublicStatus(userId, voicepackId, request.isPublic)
            ResponseEntity.ok(updatedVoicepackDto)
        } catch (e: IllegalArgumentException) {
            logger.warn("보이스팩 공개 여부 변경 실패 (잘못된 요청): voicepackId={}, error={}", voicepackId, e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: NoSuchElementException) {
            logger.warn("보이스팩 공개 여부 변경 실패 (찾을 수 없음): voicepackId={}, error={}", voicepackId, e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (e.message ?: "보이스팩을 찾을 수 없습니다.")))
        } catch (e: SecurityException) {
            logger.warn("보이스팩 공개 여부 변경 권한 없음: userId={}, voicepackId={}, error={}", userId, voicepackId, e.message)
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("보이스팩 공개 여부 변경 중 오류 발생: voicepackId={}, error={}", voicepackId, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "서버 처리 중 오류가 발생했습니다."))
        }
    }
} 