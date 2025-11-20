package com.openknot.user.dto

data class CredentialValidationRequest(
    val email: String,
    val password: String
)