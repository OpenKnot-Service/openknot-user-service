package com.openknot.user.dto

data class UpdateUserRequest(
    val name: String? = null,
    val profileImageUrl: String? = null,
    val description: String? = null,
    val githubLink: String? = null,
)