package com.openknot.user.service

import com.openknot.user.dto.RegisterRequest
import com.openknot.user.dto.UpdateUserRequest
import com.openknot.user.entity.User
import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import com.openknot.user.repository.UserRepository
import kotlinx.coroutines.flow.flowOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

/**
 * UserService 단위 테스트
 *
 * 테스트 전략:
 * - Repository를 MockK로 모킹하여 외부 의존성 제거
 * - 코루틴 테스트를 위해 runTest {} 블록 사용
 * - BDD 스타일의 given-when-then 패턴 적용
 * - Happy path와 Error handling 시나리오 모두 커버
 */
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        userService = UserService(passwordEncoder, userRepository)
    }

    @Test
    @DisplayName("getUserById - 유저가 존재할 때 정상적으로 User 엔티티를 반환한다")
    fun `given existing user id, when getUserById, then should return user entity`() = runTest {
        // given: 존재하는 유저 ID와 해당 유저 엔티티
        val userId = UUID.randomUUID()
        val expectedUser = User(
            id = userId,
            email = "test@example.com",
            password = "hashedPassword123",
            name = "테스트 유저",
            profileImageUrl = "https://example.com/profile.jpg",
            description = "테스트 설명",
            githubLink = "https://github.com/testuser",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now()
        )

        coEvery { userRepository.findById(userId) } returns expectedUser

        // when: getUserById 메서드를 호출할 때
        val result = userService.getUser(userId)

        // then: 올바른 User 엔티티가 반환되어야 한다
        result shouldNotBe null
        result.id shouldBe userId
        result.email shouldBe "test@example.com"
        result.name shouldBe "테스트 유저"
        result.profileImageUrl shouldBe "https://example.com/profile.jpg"
        result.description shouldBe "테스트 설명"
        result.githubLink shouldBe "https://github.com/testuser"

        // verify: repository의 findById가 정확히 한 번 호출되었는지 검증
        coVerify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    @DisplayName("getUserById - 유저가 존재하지 않을 때 BusinessException(USER_NOT_FOUND)을 발생시킨다")
    fun `given non-existing user id, when getUserById, then should throw BusinessException with USER_NOT_FOUND`() = runTest {
        // given: 존재하지 않는 유저 ID
        val nonExistingUserId = UUID.randomUUID()

        coEvery { userRepository.findById(nonExistingUserId) } returns null

        // when & then: getUserById 호출 시 BusinessException이 발생해야 한다
        val exception = shouldThrow<BusinessException> {
            userService.getUser(nonExistingUserId)
        }

        // then: 예외의 에러 코드가 USER_NOT_FOUND여야 한다
        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND

        // verify: repository의 findById가 정확히 한 번 호출되었는지 검증
        coVerify(exactly = 1) { userRepository.findById(nonExistingUserId) }
    }

    @Test
    @DisplayName("getUserById - 프로필 이미지와 설명이 null인 유저도 정상적으로 반환한다")
    fun `given user with null optional fields, when getUserById, then should return user entity with null fields`() = runTest {
        // given: 선택적 필드(profileImageUrl, description, githubLink)가 null인 유저
        val userId = UUID.randomUUID()
        val userWithNullFields = User(
            id = userId,
            email = "minimal@example.com",
            password = "hashedPassword456",
            name = "미니멀 유저",
            profileImageUrl = null,
            description = null,
            githubLink = null,
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now()
        )

        coEvery { userRepository.findById(userId) } returns userWithNullFields

        // when: getUserById 메서드를 호출할 때
        val result = userService.getUser(userId)

        // then: null 필드를 포함한 User 엔티티가 정상적으로 반환되어야 한다
        result shouldNotBe null
        result.id shouldBe userId
        result.email shouldBe "minimal@example.com"
        result.name shouldBe "미니멀 유저"
        result.profileImageUrl shouldBe null
        result.description shouldBe null
        result.githubLink shouldBe null

        // verify: repository의 findById가 정확히 한 번 호출되었는지 검증
        coVerify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    @DisplayName("getUserById - deleted된 유저도 조회할 수 있다 (Soft Delete)")
    fun `given deleted user id, when getUserById, then should return deleted user entity`() = runTest {
        // given: deletedAt이 설정된 유저 (Soft Delete)
        val userId = UUID.randomUUID()
        val deletedUser = User(
            id = userId,
            email = "deleted@example.com",
            password = "hashedPassword789",
            name = "삭제된 유저",
            createdAt = LocalDateTime.now().minusDays(10),
            modifiedAt = LocalDateTime.now().minusDays(5),
            deletedAt = LocalDateTime.now().minusDays(1)
        )

        coEvery { userRepository.findById(userId) } returns deletedUser

        // when: getUserById 메서드를 호출할 때
        val result = userService.getUser(userId)

        // then: deletedAt이 설정된 유저도 정상적으로 반환되어야 한다
        result shouldNotBe null
        result.id shouldBe userId
        result.deletedAt shouldNotBe null

        // verify: repository의 findById가 정확히 한 번 호출되었는지 검증
        coVerify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    @DisplayName("updateUser - 요청 필드가 주어지면 해당 필드만 갱신한 뒤 저장한다")
    fun `given valid update request, when updateUser, then should mutate only provided fields`() = runTest {
        // given
        val userId = UUID.randomUUID()
        val originalUser = User(
            id = userId,
            email = "user@example.com",
            password = "password",
            name = "Old Name",
            profileImageUrl = "https://old.example.com/profile.png",
            description = "Old description",
            githubLink = "https://github.com/old",
            createdAt = LocalDateTime.now().minusDays(10),
            modifiedAt = LocalDateTime.now().minusDays(5),
        )
        val request = UpdateUserRequest(
            name = "New Name",
            profileImageUrl = "https://new.example.com/profile.png",
            description = "New description",
            githubLink = "https://github.com/new"
        )
        coEvery { userRepository.findById(userId) } returns originalUser
        coEvery { userRepository.save(any()) } answers { firstArg() }

        // when
        val updated = userService.updateUser(userId, request)

        // then
        updated.name shouldBe "New Name"
        updated.profileImageUrl shouldBe "https://new.example.com/profile.png"
        updated.description shouldBe "New description"
        updated.githubLink shouldBe "https://github.com/new"
        updated.email shouldBe "user@example.com" // 변경되지 말아야 하는 필드
        coVerify(exactly = 1) { userRepository.findById(userId) }
        coVerify(exactly = 1) { userRepository.save(match { it.id == userId }) }
    }

    @Test
    @DisplayName("updateUser - 존재하지 않는 유저라면 USER_NOT_FOUND 예외를 던진다")
    fun `given non existing user id, when updateUser, then should throw BusinessException`() = runTest {
        // given
        val userId = UUID.randomUUID()
        val request = UpdateUserRequest(name = "should not matter")
        coEvery { userRepository.findById(userId) } returns null

        // when & then
        val exception = shouldThrow<BusinessException> {
            userService.updateUser(userId, request)
        }
        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
        coVerify(exactly = 1) { userRepository.findById(userId) }
        coVerify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    @DisplayName("updateUser - 모든 필드가 null이더라도 modifiedAt 갱신을 위해 저장한다")
    fun `given empty update request, when updateUser, then should keep original values`() = runTest {
        // given
        val userId = UUID.randomUUID()
        val originalUser = User(
            id = userId,
            email = "user@example.com",
            password = "password",
            name = "Existing Name",
            profileImageUrl = "https://example.com/profile.png",
            description = "Existing description",
            githubLink = "https://github.com/existing",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now().minusDays(1),
        )
        coEvery { userRepository.findById(userId) } returns originalUser
        coEvery { userRepository.save(any()) } answers { firstArg() }

        // when
        val updated = userService.updateUser(userId, UpdateUserRequest())

        // then
        updated.name shouldBe "Existing Name"
        updated.profileImageUrl shouldBe "https://example.com/profile.png"
        updated.description shouldBe "Existing description"
        updated.githubLink shouldBe "https://github.com/existing"
        coVerify(exactly = 1) { userRepository.save(match { it.id == userId }) }
    }

    @Test
    @DisplayName("searchUserList - 키워드와 기술스택 필터를 적용하여 Page 정보를 반환한다")
    fun `given keyword and skills, when searchUserList, then should return mapped page`() = runTest {
        // given
        val userId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 10)
        val user = User(
            id = userId,
            email = "search@example.com",
            password = "pass",
            name = "Search Target",
            profileImageUrl = null,
            description = "desc",
            githubLink = null,
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now(),
        )
        val skills = listOf(UUID.randomUUID(), UUID.randomUUID())
        every {
            userRepository.findAllUserByFilter(
                keyword = "search",
                skills = skills,
                skillsCount = skills.size,
                limit = pageable.pageSize,
                offset = pageable.offset,
            )
        } returns flowOf(user)
        coEvery { userRepository.countAllByFilter("search", skills, skills.size) } returns 1

        // when
        val page = userService.searchUserList("search", skills, pageable)

        // then
        page.totalElements shouldBe 1
        page.content.shouldNotBe(null)
        page.content.size shouldBe 1
        page.content.first().email shouldBe "search@example.com"
        page.content.first().name shouldBe "Search Target"
    }

    @Test
    @DisplayName("searchUserList - 공백 키워드와 skills=null일 때 기본 필터로 조회한다")
    fun `given blank keyword and no skills, when searchUserList, then should treat filters as optional`() = runTest {
        // given
        val pageable = PageRequest.of(2, 20)
        val user = User(
            id = UUID.randomUUID(),
            email = "blank@example.com",
            password = "pass",
            name = "Blank Keyword",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now(),
        )
        every {
            userRepository.findAllUserByFilter(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns flowOf(user)
        coEvery { userRepository.countAllByFilter(null, null, 0) } returns 1

        // when
        val page = userService.searchUserList("   ", null, pageable)

        // then
        page.content.first().email shouldBe "blank@example.com"
        verify(exactly = 1) {
            userRepository.findAllUserByFilter(
                null,
                null,
                0,
                20,
                40,
            )
        }
    }

    @Test
    @DisplayName("existsUser - 저장소 존재 여부 결과를 그대로 반환한다")
    fun `given user id, when existsUser, then should delegate to repository`() = runTest {
        // given
        val userId = UUID.randomUUID()
        coEvery { userRepository.existsById(userId) } returns true

        // when
        val exists = userService.existsUser(userId)

        // then
        exists shouldBe true
        coVerify(exactly = 1) { userRepository.existsById(userId) }
    }

    @Test
    @DisplayName("searchUserIdByCredentials - 올바른 이메일과 비밀번호로 유저 ID를 반환한다")
    fun `given valid credentials, when searchUserIdByCredentials, then should return user id`() = runTest {
        // given
        val email = "test@example.com"
        val rawPassword = "password123"
        val hashedPassword = "hashedPassword123"
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = email,
            password = hashedPassword,
            name = "Test User",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now()
        )

        coEvery { userRepository.findByEmail(email) } returns user
        every { passwordEncoder.matches(rawPassword, hashedPassword) } returns true

        // when
        val result = userService.searchUserIdByCredentials(email, rawPassword)

        // then
        result shouldBe userId
        coVerify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { passwordEncoder.matches(rawPassword, hashedPassword) }
    }

    @Test
    @DisplayName("searchUserIdByCredentials - 존재하지 않는 이메일일 때 USER_NOT_FOUND 예외를 던진다")
    fun `given non-existing email, when searchUserIdByCredentials, then should throw USER_NOT_FOUND`() = runTest {
        // given
        val email = "nonexistent@example.com"
        val password = "password123"

        coEvery { userRepository.findByEmail(email) } returns null

        // when & then
        val exception = shouldThrow<BusinessException> {
            userService.searchUserIdByCredentials(email, password)
        }

        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
        coVerify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
    }

    @Test
    @DisplayName("searchUserIdByCredentials - 비밀번호가 틀렸을 때 WRONG_PASSWORD 예외를 던진다")
    fun `given wrong password, when searchUserIdByCredentials, then should throw WRONG_PASSWORD`() = runTest {
        // given
        val email = "test@example.com"
        val wrongPassword = "wrongPassword"
        val hashedPassword = "hashedPassword123"
        val user = User(
            id = UUID.randomUUID(),
            email = email,
            password = hashedPassword,
            name = "Test User",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now()
        )

        coEvery { userRepository.findByEmail(email) } returns user
        every { passwordEncoder.matches(wrongPassword, hashedPassword) } returns false

        // when & then
        val exception = shouldThrow<BusinessException> {
            userService.searchUserIdByCredentials(email, wrongPassword)
        }

        exception.errorCode shouldBe ErrorCode.WRONG_PASSWORD
        coVerify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { passwordEncoder.matches(wrongPassword, hashedPassword) }
    }

    @Test
    @DisplayName("createUser - 새로운 유저를 정상적으로 생성하고 저장한다")
    fun `given valid register request, when createUser, then should create and save user`() = runTest {
        // given
        val request = RegisterRequest(
            email = "newuser@example.com",
            password = "rawPassword123",
            name = "New User",
            profileImageUrl = "https://example.com/profile.jpg",
            description = "New user description",
            githubLink = "https://github.com/newuser"
        )
        val hashedPassword = "hashedPassword123"

        coEvery { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns hashedPassword
        coEvery { userRepository.save(any()) } answers { firstArg() }

        // when
        val result = userService.createUser(request)

        // then
        result shouldNotBe null
        result.email shouldBe request.email
        result.password shouldBe hashedPassword
        result.name shouldBe request.name
        result.profileImageUrl shouldBe request.profileImageUrl
        result.description shouldBe request.description
        result.githubLink shouldBe request.githubLink

        coVerify(exactly = 1) { userRepository.existsByEmail(request.email) }
        verify(exactly = 1) { passwordEncoder.encode(request.password) }
        coVerify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    @DisplayName("createUser - 이미 존재하는 이메일일 때 DUPLICATE_EMAIL 예외를 던진다")
    fun `given duplicate email, when createUser, then should throw DUPLICATE_EMAIL`() = runTest {
        // given
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "password123",
            name = "Test User"
        )

        coEvery { userRepository.existsByEmail(request.email) } returns true

        // when & then
        val exception = shouldThrow<BusinessException> {
            userService.createUser(request)
        }

        exception.errorCode shouldBe ErrorCode.DUPLICATE_EMAIL
        coVerify(exactly = 1) { userRepository.existsByEmail(request.email) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        coVerify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    @DisplayName("createUser - 선택적 필드가 null인 요청도 정상적으로 처리한다")
    fun `given register request with null optional fields, when createUser, then should create user`() = runTest {
        // given
        val request = RegisterRequest(
            email = "minimal@example.com",
            password = "password123",
            name = "Minimal User",
            profileImageUrl = null,
            description = null,
            githubLink = null
        )
        val hashedPassword = "hashedPassword"

        coEvery { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns hashedPassword
        coEvery { userRepository.save(any()) } answers { firstArg() }

        // when
        val result = userService.createUser(request)

        // then
        result shouldNotBe null
        result.email shouldBe request.email
        result.name shouldBe request.name
        result.profileImageUrl shouldBe null
        result.description shouldBe null
        result.githubLink shouldBe null

        coVerify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    @DisplayName("existsUserByEmail - 이메일 존재 여부를 반환한다")
    fun `given email, when existsUserByEmail, then should return existence`() = runTest {
        // given
        val email = "test@example.com"
        coEvery { userRepository.existsByEmail(email) } returns true

        // when
        val exists = userService.existsUserByEmail(email)

        // then
        exists shouldBe true
        coVerify(exactly = 1) { userRepository.existsByEmail(email) }
    }

    @Test
    @DisplayName("existsUserByEmail - 존재하지 않는 이메일일 때 false를 반환한다")
    fun `given non-existing email, when existsUserByEmail, then should return false`() = runTest {
        // given
        val email = "nonexistent@example.com"
        coEvery { userRepository.existsByEmail(email) } returns false

        // when
        val exists = userService.existsUserByEmail(email)

        // then
        exists shouldBe false
        coVerify(exactly = 1) { userRepository.existsByEmail(email) }
    }
}
