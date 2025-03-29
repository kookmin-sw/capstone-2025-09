package kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.dto.UserSignupRequest
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.dto.UserLoginRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: UserSignupRequest): ResponseEntity<Long> {
        return ResponseEntity.ok(userService.signup(request))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: UserLoginRequest, session: HttpSession) : ResponseEntity<Long> {
        return ResponseEntity.ok(userService.login(request, session))
    }

    @GetMapping("/test")
    fun test(request: HttpServletRequest) : ResponseEntity<String> {
        val userId = request.session.getAttribute("userId") as? Long
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요함")

        return ResponseEntity.ok("로그인 완료")
    }

    @GetMapping("/test2")
    fun test2(request: HttpServletRequest): ResponseEntity<String> {
        val sessionCookie = request.cookies?.find { it.name == "JSESSIONID" }?.value
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("쿠키 없음")

        return ResponseEntity.ok("현재 쿠키 값: $sessionCookie")
    }

} 