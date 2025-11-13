package com.openknot.user.repository

import com.openknot.user.entity.TechStack
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TechStackRepository : CoroutineCrudRepository<TechStack, UUID>