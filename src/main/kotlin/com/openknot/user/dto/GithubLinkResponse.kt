package com.openknot.user.dto

import com.openknot.user.entity.UserGithub
import java.time.LocalDateTime
import java.util.UUID

data class GithubLinkResponse(
    val userGithubId: UUID, // 생성된 후 db PK
    val githubId: Long,
    val githubUsername: String,
    val linkedAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: UserGithub): GithubLinkResponse {
            return GithubLinkResponse(
                userGithubId = entity.id,
                githubId = entity.githubId,
                githubUsername = entity.githubUsername,
                linkedAt = entity.createdAt!!,
            )
        }
    }
}
