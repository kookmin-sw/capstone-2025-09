package kr.ac.kookmin.cs.capstone.voicepack_platform.user

import jakarta.servlet.http.HttpSession
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserSignupRequest
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserLoginRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserProfileDto
import java.util.NoSuchElementException
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
@Tag(name = "사용자", description = "사용자 관련 API")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/signup", consumes = ["multipart/form-data"])
    fun signup(
        @RequestParam("email") email: String,
        @RequestParam("password") password: String,
        @RequestParam("name") name: String,
        @RequestPart("profileImage", required = false) profileImage: MultipartFile?
    ): ResponseEntity<Long> {
        val request = UserSignupRequest(email, password, name, profileImage)
        return ResponseEntity.ok(userService.signup(request))
    }


    @PostMapping("/login")
    fun login(@Valid @RequestBody request: UserLoginRequest, session: HttpSession): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(userService.login(request, session))
        } catch (e: IllegalArgumentException) {
            // 비밀번호가 틀린 경우와 이메일이 틀린 경우를 구분하여 처리
            val status = if (e.message == "비밀번호가 틀렸습니다.") {
                HttpStatus.UNAUTHORIZED // 비밀번호 오류
            } else {
                HttpStatus.BAD_REQUEST // 이메일 오류
            }
            ResponseEntity.status(status).body(mapOf("error" to e.message)) // 오류 메시지를 응답 본문에 포함
        }
    }

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 정보와 활동 통계를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = UserProfileDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @GetMapping("/me/{userId}")
    fun getMyProfile(
        @Parameter(description = "조회할 사용자 ID") @PathVariable userId: Long
    ): ResponseEntity<Any> {
        return try {
            val userProfile = userService.getUserProfile(userId)
            ResponseEntity.ok(userProfile)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "프로필 조회 중 오류 발생"))
        }
    }
} 