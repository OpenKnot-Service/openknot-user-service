package com.openknot.user.service

import com.openknot.user.config.SecurityConfig
import com.openknot.user.dto.RegisterRequest
import com.openknot.user.dto.UpdateUserRequest
import com.openknot.user.dto.UserInfoResponse
import com.openknot.user.entity.User
import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import com.openknot.user.repository.UserRepository
import com.openknot.user.utils.UUIDv7
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional(readOnly = true)
@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    suspend fun searchUserIdByCredentials(
        email: String,
        password: String
    ): UUID {
        val user = userRepository.findByEmail(email)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (passwordEncoder.matches(password, user.password)) {
            return user.id
        }
        throw BusinessException(ErrorCode.WRONG_PASSWORD)
    }

    @Transactional(readOnly = false)
    suspend fun createUser(
        userData: RegisterRequest,
    ): User {
        return userRepository.save(
            User(
                id = UUIDv7.randomUUID(),
                email = userData.email,
                password = passwordEncoder.encode(userData.password),
                name = userData.name,
                profileImageUrl = userData.profileImageUrl,
                description = userData.description,
                githubLink = userData.githubLink,
            )
        )
    }

    @Transactional(readOnly = true)
    suspend fun getUser(userId: UUID): User {
        return userRepository.findById(userId) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    @Transactional(readOnly = false)
    suspend fun updateUser(
        userId: UUID,
        request: UpdateUserRequest,
    ): User {
        val user = getUser(userId)
        user.update(
            name = request.name,
            profileImageUrl = request.profileImageUrl,
            description = request.description,
            githubLink = request.githubLink,
        )

        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    suspend fun searchUserList(
        query: String? = null,
        skills: List<UUID>? = null,
        pageable: Pageable,
    ): Page<UserInfoResponse> {
        val keyword = query?.takeIf { it.isNotBlank() }
        val userList = userRepository.findAllUserByFilter(
            keyword = keyword,
            skills = skills,
            skillsCount = skills?.size ?: 0,
            limit = pageable.pageSize,
            offset = pageable.offset,
        ).toList()
        val total = userRepository.countAllByFilter(keyword, skills, skills?.size ?: 0)

        return PageImpl(
            userList.map { UserInfoResponse.fromEntity(it) },
            pageable,
            total
        )
    }

    @Transactional(readOnly = true)
    suspend fun existsUser(userId: UUID): Boolean = userRepository.existsById(userId)

    @Transactional(readOnly = true)
    suspend fun existsUserByEmail(email: String): Boolean = userRepository.existsByEmail(email)

    private suspend fun checkUserExists(email: String) {
        if (existsUserByEmail(email)) throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
    }
}
