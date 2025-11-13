package com.openknot.user.dto

import com.openknot.user.entity.User
import java.time.LocalDateTime

data class UserInfoResponse(
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val description: String? = null,
    val githubLink: String? = null,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: User): UserInfoResponse {
            return UserInfoResponse(
                email = entity.email,
                name = entity.name,
                profileImageUrl = entity.profileImageUrl,
                description = entity.description,
                githubLink = entity.githubLink,
                createdAt = entity.createdAt!!,
            )
        }
    }
}
