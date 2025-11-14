package com.openknot.user.controller

import com.ninjasquad.springmockk.MockkBean
import com.openknot.user.dto.UpdateUserRequest
import com.openknot.user.dto.UserInfoResponse
import com.openknot.user.entity.User
import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import com.openknot.user.repository.TechStackRepository
import com.openknot.user.repository.UserRepository
import com.openknot.user.repository.UserTechStackRepository
import com.openknot.user.service.TechStackService
import com.openknot.user.service.UserService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

/**
 * UserController 통합 테스트
 *
 * 테스트 전략:
 * - @WebFluxTest를 사용하여 Controller 레이어만 로드 (빠른 테스트)
 * - UserService를 MockK로 모킹
 * - WebTestClient로 HTTP 요청/응답 검증
 * - 성공 케이스와 실패 케이스 모두 테스트
 * - ExceptionHandler의 동작도 함께 검증
 */
@WebFluxTest(
    controllers = [UserController::class],
    excludeAutoConfiguration = [
        R2dbcAutoConfiguration::class,
        R2dbcDataAutoConfiguration::class,
        R2dbcRepositoriesAutoConfiguration::class,
    ],
)
@Import(
    com.openknot.user.handler.ExceptionHandler::class,
    com.openknot.user.converter.StringUuidConverter::class,
    UserControllerTest.TestConfig::class,
)
@ActiveProfiles("test")
@DisplayName("UserController 통합 테스트")
class UserControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun r2dbcEntityTemplate(): R2dbcEntityTemplate = mockk(relaxed = true)

        @Bean
        fun r2dbcMappingContext(): R2dbcMappingContext = R2dbcMappingContext()

        @Bean
        fun reactivePageableResolver(): ReactivePageableHandlerMethodArgumentResolver =
            ReactivePageableHandlerMethodArgumentResolver()

        @Bean
        fun pageableWebFluxConfigurer(
            pageableResolver: ReactivePageableHandlerMethodArgumentResolver,
        ): WebFluxConfigurer = object : WebFluxConfigurer {
            override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
                configurer.addCustomResolver(pageableResolver)
            }
        }
    }

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean(relaxUnitFun = true)
    private lateinit var userRepository: UserRepository

    @MockkBean(relaxUnitFun = true)
    private lateinit var techStackRepository: TechStackRepository

    @MockkBean(relaxUnitFun = true)
    private lateinit var userTechStackRepository: UserTechStackRepository

    @MockkBean(relaxUnitFun = true)
    private lateinit var techStackService: TechStackService

    @Test
    @DisplayName("GET /users/me - 유저가 존재할 때 200 OK와 유저 정보를 반환한다")
    fun `given existing user id, when get current user, then should return 200 with user info`() {
        // given: 존재하는 유저 ID와 해당 유저 엔티티
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "test@example.com",
            password = "hashedPassword123",
            name = "테스트 유저",
            profileImageUrl = "https://example.com/profile.jpg",
            description = "테스트 설명",
            githubLink = "https://github.com/testuser",
            createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
            modifiedAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
        )

        coEvery { userService.getUser(userId) } returns user

        // when & then: GET /users/me 요청 시 200 OK와 유저 정보가 반환되어야 한다
        webTestClient.get()
            .uri("/users/me?id={id}", userId.toString())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").isEqualTo("test@example.com")
            .jsonPath("$.name").isEqualTo("테스트 유저")
            .jsonPath("$.profileImageUrl").isEqualTo("https://example.com/profile.jpg")
            .jsonPath("$.description").isEqualTo("테스트 설명")
            .jsonPath("$.githubLink").isEqualTo("https://github.com/testuser")
            .jsonPath("$.createdAt").isNotEmpty
            .jsonPath("$.password").doesNotExist()  // 비밀번호는 응답에 포함되지 않아야 함

        // verify: userService.getUserById가 호출되었는지 검증
        coVerify(exactly = 1) { userService.getUser(userId) }
    }

    @Test
    @DisplayName("GET /users/me - 유저가 존재하지 않을 때 404 NOT_FOUND를 반환한다")
    fun `given non-existing user id, when get current user, then should return 404`() {
        // given: 존재하지 않는 유저 ID
        val nonExistingUserId = UUID.randomUUID()

        coEvery { userService.getUser(nonExistingUserId) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

        // when & then: GET /users/me 요청 시 404와 에러 응답이 반환되어야 한다
        webTestClient.get()
            .uri("/users/me?id={id}", nonExistingUserId.toString())
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.code").isEqualTo("USER.001")
            .jsonPath("$.message").isEqualTo("사용자를 찾을 수 없음")

        // verify: userService.getUserById가 호출되었는지 검증
        coVerify(exactly = 1) { userService.getUser(nonExistingUserId) }
    }

    @Test
    @DisplayName("GET /users/me - 잘못된 UUID 형식일 때 400 BAD_REQUEST를 반환한다")
    fun `given invalid uuid format, when get current user, then should return 400`() {
        // given: 잘못된 UUID 형식
        val invalidUuidString = "invalid-uuid-format"

        // when & then: GET /users/me 요청 시 400과 에러 응답이 반환되어야 한다
        webTestClient.get()
            .uri("/users/me?id={id}", invalidUuidString)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.error").isEqualTo("Bad Request")

        // verify: userService.getUserById는 호출되지 않아야 함 (Converter에서 예외 발생)
        coVerify(exactly = 0) { userService.getUser(any()) }
    }

    @Test
    @DisplayName("GET /users/me - id 파라미터가 없을 때 400 BAD_REQUEST를 반환한다")
    fun `given missing id parameter, when get current user, then should return 400`() {
        // when & then: id 파라미터 없이 GET /users/me 요청 시 400이 반환되어야 한다
        webTestClient.get()
            .uri("/users/me")
            .exchange()
            .expectStatus().isBadRequest

        // verify: userService.getUserById는 호출되지 않아야 함
        coVerify(exactly = 0) { userService.getUser(any()) }
    }

    @Test
    @DisplayName("GET /users/me - 선택적 필드가 null인 유저도 정상적으로 반환한다")
    fun `given user with null optional fields, when get current user, then should return 200 with null fields`() {
        // given: 선택적 필드가 null인 유저
        val userId = UUID.randomUUID()
        val userWithNullFields = User(
            id = userId,
            email = "minimal@example.com",
            password = "hashedPassword456",
            name = "미니멀 유저",
            profileImageUrl = null,
            description = null,
            githubLink = null,
            createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
            modifiedAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
        )

        coEvery { userService.getUser(userId) } returns userWithNullFields

        // when & then: GET /users/me 요청 시 200 OK와 유저 정보가 반환되어야 한다
        webTestClient.get()
            .uri("/users/me?id={id}", userId.toString())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").isEqualTo("minimal@example.com")
            .jsonPath("$.name").isEqualTo("미니멀 유저")
            .jsonPath("$.profileImageUrl").isEqualTo(null)
            .jsonPath("$.description").isEqualTo(null)
            .jsonPath("$.githubLink").isEqualTo(null)
            .jsonPath("$.createdAt").isNotEmpty

        // verify: userService.getUserById가 호출되었는지 검증
        coVerify(exactly = 1) { userService.getUser(userId) }
    }

    @Test
    @DisplayName("GET /users/me - 최소값 UUID로 유저를 정상적으로 조회한다")
    fun `given minimum uuid, when get current user, then should return 200`() {
        // given: 최소값 UUID (모든 비트 0)
        val minUuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val user = User(
            id = minUuid,
            email = "min@example.com",
            password = "password",
            name = "Min User",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now()
        )

        coEvery { userService.getUser(minUuid) } returns user

        // when & then: GET /users/me 요청 시 200 OK가 반환되어야 한다
        webTestClient.get()
            .uri("/users/me?id={id}", minUuid.toString())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").isEqualTo("min@example.com")

        // verify: userService.getUserById가 호출되었는지 검증
        coVerify(exactly = 1) { userService.getUser(minUuid) }
    }

    @Test
    @DisplayName("GET /users/me - 대문자 UUID 문자열로도 정상적으로 조회한다")
    fun `given uppercase uuid string, when get current user, then should return 200`() {
        // given: 대문자 UUID 문자열
        val userId = UUID.randomUUID()
        val uppercaseUuidString = userId.toString().uppercase()
        val user = User(
            id = userId,
            email = "upper@example.com",
            password = "password",
            name = "Upper User",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now()
        )

        coEvery { userService.getUser(userId) } returns user

        // when & then: 대문자 UUID로 GET /users/me 요청 시 200 OK가 반환되어야 한다
        webTestClient.get()
            .uri("/users/me?id={id}", uppercaseUuidString)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").isEqualTo("upper@example.com")

        // verify: userService.getUserById가 호출되었는지 검증
        coVerify(exactly = 1) { userService.getUser(userId) }
    }

    @Test
    @DisplayName("GET /users/{userId} - PathVariable로 유저 조회 시 200 OK를 반환한다")
    fun `given existing user id, when getUser endpoint called, then should return 200`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "path@example.com",
            password = "password",
            name = "Path User",
            profileImageUrl = null,
            description = "path desc",
            githubLink = null,
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now(),
        )
        coEvery { userService.getUser(userId) } returns user

        webTestClient.get()
            .uri("/users/{userId}", userId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").isEqualTo("path@example.com")
            .jsonPath("$.name").isEqualTo("Path User")

        coVerify(exactly = 1) { userService.getUser(userId) }
    }

    @Test
    @DisplayName("GET /users/{userId} - 존재하지 않는 유저면 404를 반환한다")
    fun `given non existing user id, when getUser endpoint called, then should return 404`() {
        val userId = UUID.randomUUID()
        coEvery { userService.getUser(userId) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

        webTestClient.get()
            .uri("/users/{userId}", userId)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.code").isEqualTo("USER.001")

        coVerify(exactly = 1) { userService.getUser(userId) }
    }

    @Test
    @DisplayName("PUT /users/{userId} - 요청된 필드를 업데이트하고 응답을 반환한다")
    fun `given update request, when updateUser endpoint called, then should return updated payload`() {
        val userId = UUID.randomUUID()
        val request = UpdateUserRequest(
            name = "Updated Name",
            profileImageUrl = "https://example.com/new.png",
            description = "updated desc",
            githubLink = "https://github.com/updated",
        )
        val updatedUser = User(
            id = userId,
            email = "path@example.com",
            password = "password",
            name = request.name!!,
            profileImageUrl = request.profileImageUrl,
            description = request.description,
            githubLink = request.githubLink,
            createdAt = LocalDateTime.now().minusDays(1),
            modifiedAt = LocalDateTime.now(),
        )
        val requestSlot = slot<UpdateUserRequest>()
        coEvery { userService.updateUser(userId, capture(requestSlot)) } returns updatedUser

        webTestClient.put()
            .uri("/users/{userId}", userId)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Updated Name")
            .jsonPath("$.profileImageUrl").isEqualTo("https://example.com/new.png")
            .jsonPath("$.description").isEqualTo("updated desc")
            .jsonPath("$.githubLink").isEqualTo("https://github.com/updated")

        coVerify(exactly = 1) { userService.updateUser(userId, any()) }
        assertEquals("Updated Name", requestSlot.captured.name)
        assertEquals("https://example.com/new.png", requestSlot.captured.profileImageUrl)
        assertEquals("updated desc", requestSlot.captured.description)
        assertEquals("https://github.com/updated", requestSlot.captured.githubLink)
    }

    @Test
    @DisplayName("PUT /users/{userId} - 대상 유저가 없으면 404를 반환한다")
    fun `given non existing user id, when updateUser endpoint called, then should return 404`() {
        val userId = UUID.randomUUID()
        coEvery { userService.updateUser(userId, any()) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

        webTestClient.put()
            .uri("/users/{userId}", userId)
            .bodyValue(UpdateUserRequest(name = "fail"))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.code").isEqualTo("USER.001")

        coVerify(exactly = 1) { userService.updateUser(userId, any()) }
    }

    @Test
    @DisplayName("GET /users/search - 쿼리와 스킬 필터를 적용하여 페이지 데이터를 반환한다")
    fun `given query and skill filters, when searchUser endpoint called, then should return paged response`() {
        val pageable = PageRequest.of(1, 2)
        val skills = listOf(UUID.randomUUID(), UUID.randomUUID())
        val responses = listOf(
            UserInfoResponse(
                email = "java@example.com",
                name = "Java Dev",
                profileImageUrl = null,
                description = "desc1",
                githubLink = null,
                createdAt = LocalDateTime.now(),
            ),
            UserInfoResponse(
                email = "kotlin@example.com",
                name = "Kotlin Dev",
                profileImageUrl = null,
                description = "desc2",
                githubLink = null,
                createdAt = LocalDateTime.now(),
            )
        )
        val pageableSlot = slot<Pageable>()
        coEvery {
            userService.searchUserList(
                query = any(),
                skills = any(),
                pageable = capture(pageableSlot),
            )
        } returns PageImpl(responses, pageable, 5)

        webTestClient.get()
            .uri {
                it.path("/users/search")
                    .queryParam("q", "java")
                    .queryParam("skills", skills[0])
                    .queryParam("skills", skills[1])
                    .queryParam("page", "1")
                    .queryParam("size", "2")
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content[0].email").isEqualTo("java@example.com")
            .jsonPath("$.content[1].name").isEqualTo("Kotlin Dev")
            .jsonPath("$.totalElements").isEqualTo(5)

        coVerify(exactly = 1) { userService.searchUserList("java", skills, any()) }
        assertEquals(1, pageableSlot.captured.pageNumber)
        assertEquals(2, pageableSlot.captured.pageSize)
    }
}
