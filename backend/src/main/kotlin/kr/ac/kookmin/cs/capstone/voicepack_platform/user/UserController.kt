package kr.ac.kookmin.cs.capstone.voicepack_platform.user

import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserSignupRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid

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
} 