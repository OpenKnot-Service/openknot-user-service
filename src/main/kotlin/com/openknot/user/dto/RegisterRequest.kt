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
    @field:NotBlank
    val profileImageUrl: String? = null,
    @field:NotBlank
    val description: String? = null,
    @field:NotBlank
    val githubLink: String? = null,
)
