package com.openknot.user.repository

import com.openknot.user.entity.UserGithub
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserGithubRepository : CoroutineCrudRepository<UserGithub, UUID> {
    suspend fun existsByGithubId(githubId: Long): Boolean
    suspend fun findByUserId(userId: UUID): UserGithub?
    suspend fun findByGithubId(githubId: Long): UserGithub?
}
