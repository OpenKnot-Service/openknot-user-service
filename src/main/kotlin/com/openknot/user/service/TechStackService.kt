package com.openknot.user.service

import com.openknot.user.entity.UserTechStack
import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import com.openknot.user.repository.TechStackRepository
import com.openknot.user.repository.UserTechStackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional(readOnly = true)
@Service
class TechStackService(
    private val techStackRepository: TechStackRepository,
    private val userTechStackRepository: UserTechStackRepository,
    private val userService: UserService,
) {

    @Transactional(readOnly = true)
    suspend fun getUserTechStackList(
        userId: UUID,
    ): List<UserTechStack> {
        // 유저가 없을 경우 에러 반환
        if (!userService.existsUser(userId)) throw BusinessException(ErrorCode.USER_NOT_FOUND)

        return userTechStackRepository.findAllByUserId(userId)
    }

}