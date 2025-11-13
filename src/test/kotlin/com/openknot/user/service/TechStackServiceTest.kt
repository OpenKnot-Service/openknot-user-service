package com.openknot.user.service

import com.openknot.user.entity.UserTechStack
import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import com.openknot.user.repository.TechStackRepository
import com.openknot.user.repository.UserTechStackRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

@DisplayName("TechStackService 단위 테스트")
class TechStackServiceTest {

    private lateinit var techStackRepository: TechStackRepository
    private lateinit var userTechStackRepository: UserTechStackRepository
    private lateinit var userService: UserService
    private lateinit var techStackService: TechStackService

    @BeforeEach
    fun setUp() {
        techStackRepository = mockk(relaxed = true)
        userTechStackRepository = mockk()
        userService = mockk()
        techStackService = TechStackService(techStackRepository, userTechStackRepository, userService)
    }

    @Test
    @DisplayName("getUserTechStackList - 유저가 존재하면 기술 스택 목록을 반환한다")
    fun `given existing user id, when getUserTechStackList, then should return tech stacks`() = runTest {
        // given
        val userId = UUID.randomUUID()
        val techStacks = listOf(
            UserTechStack(
                id = UUID.randomUUID(),
                userId = userId,
                techStackId = UUID.randomUUID(),
                createdAt = LocalDateTime.now(),
            ),
            UserTechStack(
                id = UUID.randomUUID(),
                userId = userId,
                techStackId = UUID.randomUUID(),
                createdAt = LocalDateTime.now(),
            )
        )
        coEvery { userService.existsUser(userId) } returns true
        coEvery { userTechStackRepository.findAllByUserId(userId) } returns techStacks

        // when
        val result = techStackService.getUserTechStackList(userId)

        // then
        result.shouldContainExactly(techStacks)
        coVerify(exactly = 1) { userService.existsUser(userId) }
        coVerify(exactly = 1) { userTechStackRepository.findAllByUserId(userId) }
    }

    @Test
    @DisplayName("getUserTechStackList - 존재하지 않는 유저면 USER_NOT_FOUND 예외를 던진다")
    fun `given non existing user id, when getUserTechStackList, then should throw BusinessException`() = runTest {
        // given
        val userId = UUID.randomUUID()
        coEvery { userService.existsUser(userId) } returns false

        // when & then
        val exception = shouldThrow<BusinessException> {
            techStackService.getUserTechStackList(userId)
        }
        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
        coVerify(exactly = 1) { userService.existsUser(userId) }
        coVerify(exactly = 0) { userTechStackRepository.findAllByUserId(userId) }
    }
}
