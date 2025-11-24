package com.openknot.user.service

import com.openknot.user.dto.GithubLinkRequest
import com.openknot.user.dto.RegisterRequest
import com.openknot.user.dto.UpdateUserRequest
import com.openknot.user.dto.UserInfoResponse
import com.openknot.user.entity.User
import com.openknot.user.entity.UserGithub
import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import com.openknot.user.repository.UserGithubRepository
import com.openknot.user.repository.UserRepository
import com.openknot.user.utils.UUIDv7
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional(readOnly = true)
@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val userGithubRepository: UserGithubRepository,
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

    @Transactional(readOnly = false)
    suspend fun githubLink(
        currentUserId: UUID,
        githubLinkData: GithubLinkRequest,
    ): UserGithub {
        // 1. current User랑 동일한지 확인
        if (currentUserId != githubLinkData.userId) throw BusinessException(ErrorCode.OAUTH_ACCOUNT_MISMATCH)

        // 2. 이미 다른 사용자가 등록한 githubId인지 확인 (본인이 이미 연동한 경우는 제외)
        val existingGithubLink = userGithubRepository.findByGithubId(githubLinkData.githubId)
        if (existingGithubLink != null && existingGithubLink.userId != currentUserId) {
            throw BusinessException(ErrorCode.OAUTH_DUPLICATE_ACCOUNT)
        }

        // 3. 계정 연동 (본인계정에 다른 GitHub 계정이 등록되있으면 업데이트로 처리)
        val userGithub = userGithubRepository.findByUserId(currentUserId)?.apply {
            // 이미 연동을 했으나 다시 Link 하려는 경우
            githubId = githubLinkData.githubId
            githubUsername = githubLinkData.githubUsername
            githubAccessToken = githubLinkData.githubAccessToken
            avatarUrl = githubLinkData.avatarUrl
            modifiedAt = LocalDateTime.now()
        } ?: githubLinkData.toEntity()

        return userGithubRepository.save(userGithub)
    }

    @Transactional(readOnly = true)
    suspend fun existsUser(userId: UUID): Boolean = userRepository.existsById(userId)

    @Transactional(readOnly = true)
    suspend fun existsUserByEmail(email: String): Boolean = userRepository.existsByEmail(email)

    private suspend fun checkUserExists(email: String) {
        if (existsUserByEmail(email)) throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
    }
}
