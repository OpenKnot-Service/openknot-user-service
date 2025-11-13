package com.openknot.user.repository

import com.openknot.user.entity.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {
    @Query("""
        SELECT DISTINCT
            u.id, u.email, u.password, u.name, u.profile_image_url, u.description,
            u.github_link, u.created_at, u.modified_at, u.deleted_at
        FROM `user` u
        WHERE ( -- 키워드 없으면 OFF, 있으면 name/email LIKE
            :keyword IS NULL
            OR u.name LIKE CONCAT('%', :keyword, '%')
            OR u.email LIKE CONCAT('%', :keyword, '%')
        ) AND ( -- skillsCount=0이면 OFF, >0이면 "모든 기술 스택 포함(AND)" 검사
            :skillsCount = 0
            OR EXISTS (
                SELECT 1
                FROM user_tech_stack uts
                WHERE uts.user_id = u.id
                    AND uts.tech_stack_id IN (:skills)
                GROUP BY uts.user_id
                HAVING COUNT(DISTINCT uts.tech_stack_id) = :skillsCount
            )
        )
        ORDER BY u.name, u.email, u.id
        LIMIT :limit OFFSET :offset
    """
    )
    fun findAllUserByFilter(
        keyword: String?,
        skills: List<UUID>?,
        skillsCount: Int,
        limit: Int,
        offset: Long
    ): Flow<User>

    @Query("""
        SELECT COUNT(*)
        FROM `user` u
        WHERE (
            :keyword IS NULL
            OR u.name LIKE CONCAT('%', :keyword, '%')
            OR u.email LIKE CONCAT('%', :keyword, '%')
        ) AND (
            :skillsCount = 0
            OR EXISTS (
                SELECT 1
                FROM user_tech_stack uts
                WHERE uts.user_id = u.id
                    AND uts.tech_stack_id IN (:skills)
                GROUP BY uts.user_id
                HAVING COUNT(DISTINCT uts.tech_stack_id) = :skillsCount)
            )
        )
    """)
    suspend fun countAllByFilter(
        keyword: String?,
        skills: List<UUID>?,
        skillsCount: Int,
    ): Long
}