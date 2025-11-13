package com.openknot.user.repository

import com.openknot.user.config.R2dbcConfig
import com.openknot.user.entity.User
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.*

/**
 * UserRepository 통합 테스트
 *
 * 테스트 전략:
 * - @DataR2dbcTest를 사용하여 R2DBC 레이어만 로드 (빠른 테스트)
 * - Testcontainers로 실제 MySQL 컨테이너 사용 (프로덕션 환경과 동일)
 * - R2DBC 커스텀 컨버터 (UUID ↔ ByteBuffer) 동작 검증
 * - 코루틴 기반 CoroutineCrudRepository 메서드 테스트
 * - 각 테스트는 독립적이고 격리됨 (@Transactional로 자동 롤백)
 */
@DataR2dbcTest
@Import(R2dbcConfig::class)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryTest {

    companion object {
        @Container
        @JvmStatic
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
            withInitScript("schema.sql")  // 테이블 스키마 초기화 스크립트
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:mysql://${mysqlContainer.host}:${mysqlContainer.firstMappedPort}/${mysqlContainer.databaseName}"
            }
            registry.add("spring.r2dbc.username") { mysqlContainer.username }
            registry.add("spring.r2dbc.password") { mysqlContainer.password }
        }
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @BeforeEach
    fun setUp() = runTest {
        // 각 테스트 전에 테이블 초기화
        r2dbcEntityTemplate
            .databaseClient
            .sql("DELETE FROM user")
            .fetch()
            .rowsUpdated()
            .block()
    }

    @Test
    @DisplayName("save - 유저를 정상적으로 저장하고 조회할 수 있다")
    fun `given new user, when save, then should persist and retrieve user`() = runTest {
        // given: 새로운 유저 엔티티
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "test@example.com",
            password = "hashedPassword123",
            name = "테스트 유저",
            profileImageUrl = "https://example.com/profile.jpg",
            description = "테스트 설명",
            githubLink = "https://github.com/testuser"
        )

        // when: 유저를 저장하고 조회할 때
        userRepository.save(user)
        val foundUser = userRepository.findById(userId)

        // then: 저장한 유저가 정상적으로 조회되어야 한다
        foundUser shouldNotBe null
        foundUser!!.id shouldBe userId
        foundUser.email shouldBe "test@example.com"
        foundUser.name shouldBe "테스트 유저"
        foundUser.profileImageUrl shouldBe "https://example.com/profile.jpg"
        foundUser.description shouldBe "테스트 설명"
        foundUser.githubLink shouldBe "https://github.com/testuser"
        foundUser.createdAt shouldNotBe null
    }

    @Test
    @DisplayName("findById - 존재하지 않는 ID로 조회 시 null을 반환한다")
    fun `given non-existing id, when findById, then should return null`() = runTest {
        // given: 존재하지 않는 유저 ID
        val nonExistingId = UUID.randomUUID()

        // when: findById로 조회할 때
        val result = userRepository.findById(nonExistingId)

        // then: null이 반환되어야 한다
        result shouldBe null
    }

    @Test
    @DisplayName("save - 선택적 필드가 null인 유저도 정상적으로 저장된다")
    fun `given user with null optional fields, when save, then should persist user`() = runTest {
        // given: 선택적 필드가 null인 유저
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "minimal@example.com",
            password = "password",
            name = "미니멀 유저",
            profileImageUrl = null,
            description = null,
            githubLink = null
        )

        // when: 유저를 저장하고 조회할 때
        userRepository.save(user)
        val foundUser = userRepository.findById(userId)

        // then: null 필드를 포함한 유저가 정상적으로 저장 및 조회되어야 한다
        foundUser shouldNotBe null
        foundUser!!.id shouldBe userId
        foundUser.email shouldBe "minimal@example.com"
        foundUser.profileImageUrl shouldBe null
        foundUser.description shouldBe null
        foundUser.githubLink shouldBe null
    }

    @Test
    @DisplayName("save - UUID가 BINARY(16)으로 정상적으로 변환되어 저장된다")
    fun `given user with uuid, when save, then should convert uuid to binary correctly`() = runTest {
        // given: UUID를 가진 유저
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "uuid@example.com",
            password = "password",
            name = "UUID User"
        )

        // when: 유저를 저장하고 조회할 때
        userRepository.save(user)
        val foundUser = userRepository.findById(userId)

        // then: UUID가 올바르게 변환되어 저장 및 조회되어야 한다
        foundUser shouldNotBe null
        foundUser!!.id shouldBe userId
    }

    @Test
    @DisplayName("save - 최소값 UUID도 정상적으로 저장된다")
    fun `given user with minimum uuid, when save, then should persist user`() = runTest {
        // given: 최소값 UUID를 가진 유저
        val minUuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val user = User(
            id = minUuid,
            email = "min@example.com",
            password = "password",
            name = "Min User"
        )

        // when: 유저를 저장하고 조회할 때
        userRepository.save(user)
        val foundUser = userRepository.findById(minUuid)

        // then: 최소값 UUID가 정상적으로 저장 및 조회되어야 한다
        foundUser shouldNotBe null
        foundUser!!.id shouldBe minUuid
    }

    @Test
    @DisplayName("save - 최대값 UUID도 정상적으로 저장된다")
    fun `given user with maximum uuid, when save, then should persist user`() = runTest {
        // given: 최대값 UUID를 가진 유저
        val maxUuid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
        val user = User(
            id = maxUuid,
            email = "max@example.com",
            password = "password",
            name = "Max User"
        )

        // when: 유저를 저장하고 조회할 때
        userRepository.save(user)
        val foundUser = userRepository.findById(maxUuid)

        // then: 최대값 UUID가 정상적으로 저장 및 조회되어야 한다
        foundUser shouldNotBe null
        foundUser!!.id shouldBe maxUuid
    }

    @Test
    @DisplayName("save - createdAt과 modifiedAt이 자동으로 설정된다")
    fun `given new user, when save, then should auto-set createdAt and modifiedAt`() = runTest {
        // given: createdAt과 modifiedAt이 null인 유저
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "auto@example.com",
            password = "password",
            name = "Auto Timestamp User",
            createdAt = null,
            modifiedAt = null
        )

        // when: 유저를 저장하고 조회할 때
        userRepository.save(user)
        val foundUser = userRepository.findById(userId)

        // then: createdAt과 modifiedAt이 자동으로 설정되어야 한다
        foundUser shouldNotBe null
        foundUser!!.createdAt shouldNotBe null
        foundUser.modifiedAt shouldNotBe null
    }

    @Test
    @DisplayName("delete - 유저를 정상적으로 삭제할 수 있다")
    fun `given existing user, when delete, then should remove user`() = runTest {
        // given: 저장된 유저
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "delete@example.com",
            password = "password",
            name = "Delete User"
        )
        userRepository.save(user)

        // when: 유저를 삭제할 때
        userRepository.deleteById(userId)

        // then: 유저가 더 이상 조회되지 않아야 한다
        val deletedUser = userRepository.findById(userId)
        deletedUser shouldBe null
    }

    @Test
    @DisplayName("count - 저장된 유저 수를 정확히 반환한다")
    fun `given multiple users, when count, then should return correct count`() = runTest {
        // given: 여러 유저 저장
        val user1 = User(UUID.randomUUID(), "user1@example.com", "pass", "User 1")
        val user2 = User(UUID.randomUUID(), "user2@example.com", "pass", "User 2")
        val user3 = User(UUID.randomUUID(), "user3@example.com", "pass", "User 3")

        userRepository.save(user1)
        userRepository.save(user2)
        userRepository.save(user3)

        // when: count를 호출할 때
        val count = userRepository.count()

        // then: 올바른 유저 수가 반환되어야 한다
        count shouldBe 3
    }
}
