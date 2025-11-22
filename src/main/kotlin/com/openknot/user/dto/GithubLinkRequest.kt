package com.openknot.user.dto

import com.openknot.user.entity.UserGithub
import com.openknot.user.utils.UUIDv7
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class GithubLinkRequest(
    @field:NotNull
    val userId: UUID,
    @field:NotNull
    val githubId: Long,
    @field:NotBlank
    val githubUsername: String,
    @field:NotBlank
    val githubAccessToken: String,
    val avatarUrl: String?,
) {
    fun toEntity() = UserGithub(
        id = UUIDv7.randomUUID(),
        userId = userId,
        githubId = githubId,
        githubUsername = githubUsername,
        githubAccessToken = githubAccessToken,
        avatarUrl = avatarUrl,
    )
}
