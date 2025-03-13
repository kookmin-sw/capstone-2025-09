package kr.ac.kookmin.cs.capstone.voicepack_platform.user

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserSignupRequest
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserLoginRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: UserSignupRequest): ResponseEntity<Long> {
        val userId = userService.signup(request)
        return ResponseEntity.ok(userId)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: UserLoginRequest, session: HttpSession) : ResponseEntity<String> {
        userService.login(request, session)
        return ResponseEntity.ok("로그인 성공")
    }

    @GetMapping("/test")
    fun test(request: HttpServletRequest) : ResponseEntity<String> {
        val userId = request.session.getAttribute("userId") as? Long
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요함")

        return ResponseEntity.ok("로그인 완료")
    }
} 