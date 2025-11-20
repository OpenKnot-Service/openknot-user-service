package com.openknot.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegisterRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
    @field:NotBlank
    val name: String,
    val profileImageUrl: String? = null,
    val description: String? = null,
    val githubLink: String? = null,
)
