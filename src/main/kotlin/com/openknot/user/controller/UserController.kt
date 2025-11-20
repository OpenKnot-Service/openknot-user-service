package com.openknot.user.controller

import com.openknot.user.dto.CredentialValidationRequest
import com.openknot.user.dto.RegisterRequest
import com.openknot.user.dto.UpdateUserRequest
import com.openknot.user.dto.UserIdResponse
import com.openknot.user.dto.UserInfoResponse
import com.openknot.user.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/validate-credentials")
    suspend fun validateCredentials(
        @RequestBody request: CredentialValidationRequest,
    ): ResponseEntity<UserIdResponse> {
        return ResponseEntity.ok(
            UserIdResponse(userService.searchUserIdByCredentials(request.email, request.password))
        )
    }

    @PostMapping("/register")
    suspend fun registerUser(
        @RequestBody @Valid request: RegisterRequest,
    ): ResponseEntity<UserInfoResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UserInfoResponse.fromEntity(userService.createUser(request)))
    }

    @GetMapping("/me")
    suspend fun getCurrentUser(
        @RequestParam(value = "id", required = true) id: UUID, // TODO: [DEV] gateway 구현 이후 토큰으로 변경 예정
    ): ResponseEntity<UserInfoResponse> {
        return ResponseEntity.ok(
            UserInfoResponse.fromEntity(userService.getUser(id)),
        )
    }

    @GetMapping("/{userId}")
    suspend fun getUserById(
        @PathVariable userId: UUID,
    ): ResponseEntity<UserInfoResponse> {
        return ResponseEntity.ok(
            UserInfoResponse.fromEntity(userService.getUser(userId)),
        )
    }

    @PutMapping("/{userId}")
    suspend fun updateUser(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateUserRequest,
    ): ResponseEntity<UserInfoResponse> {
        return ResponseEntity.ok(
            UserInfoResponse.fromEntity(userService.updateUser(userId, request))
        )
    }

    @GetMapping("/search")
    suspend fun searchUser(
        @RequestParam(value = "q", required = true) query: String,
        @RequestParam(value = "skills", required = false) skills: List<UUID>?,
        pageable: Pageable,
    ): ResponseEntity<Page<UserInfoResponse>> {
        return ResponseEntity.ok(
            userService.searchUserList(query, skills, pageable)
        )
    }
}