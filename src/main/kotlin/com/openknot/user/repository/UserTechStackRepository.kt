package com.openknot.user.repository

import com.openknot.user.entity.UserTechStack
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserTechStackRepository : CoroutineCrudRepository<UserTechStack, UUID> {
    suspend fun findAllByUserId(userId: UUID): List<UserTechStack>
}