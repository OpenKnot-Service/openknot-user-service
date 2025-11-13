package com.openknot.user.dto

import com.openknot.user.entity.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

@DisplayName("UserInfoResponse.fromEntity 단위 테스트")
class UserInfoResponseTest {

    @Test
    @DisplayName("모든 필드가 채워진 User 엔티티를 정확히 변환한다")
    fun `given full user entity, when fromEntity, then should map every field`() {
        val createdAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
        val user = User(
            id = UUID.randomUUID(),
            email = "full@example.com",
            password = "hashed",
            name = "전체 유저",
            profileImageUrl = "https://example.com/profile.png",
            description = "소개",
            githubLink = "https://github.com/full",
            createdAt = createdAt,
            modifiedAt = createdAt.plusDays(1)
        )

        val response = UserInfoResponse.fromEntity(user)

        response.email shouldBe user.email
        response.name shouldBe user.name
        response.profileImageUrl shouldBe user.profileImageUrl
        response.description shouldBe user.description
        response.githubLink shouldBe user.githubLink
        response.createdAt shouldBe createdAt
    }

    @Test
    @DisplayName("선택 필드가 null인 경우에도 null 그대로 노출된다")
    fun `given user with null optional fields, when fromEntity, then should keep null values`() {
        val createdAt = LocalDateTime.now()
        val user = User(
            id = UUID.randomUUID(),
            email = "minimal@example.com",
            password = "hashed",
            name = "미니멀",
            profileImageUrl = null,
            description = null,
            githubLink = null,
            createdAt = createdAt,
            modifiedAt = createdAt
        )

        val response = UserInfoResponse.fromEntity(user)

        response.profileImageUrl shouldBe null
        response.description shouldBe null
        response.githubLink shouldBe null
        response.createdAt shouldBe createdAt
    }

    @Test
    @DisplayName("createdAt이 null인 엔티티는 변환 시 NullPointerException을 던진다")
    fun `given user without createdAt, when fromEntity, then should throw NullPointerException`() {
        val user = User(
            id = UUID.randomUUID(),
            email = "invalid@example.com",
            password = "hashed",
            name = "잘못된",
            createdAt = null,
            modifiedAt = null
        )

        shouldThrow<NullPointerException> {
            UserInfoResponse.fromEntity(user)
        }
    }
}
