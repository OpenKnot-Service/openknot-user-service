package com.openknot.user.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class UpdateUserRequest(
    @field:Pattern(
        regexp = "^(?!\\s*$).+",
        message = "이름은 빈 문자열일 수 없습니다"
    )
    @field:Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다")
    val name: String? = null,

    val position: String? = null,

    @field:Size(min = 1, max = 30, message = "전문 분야는 1자 이상 30자 이하여야 합니다.")
    val detailedPosition: String? = null,

    val careerLevel: String? = null,

    @field:URL(message = "올바른 URL 형식이 아닙니다")
    val profileImageUrl: String? = null,

    @field:Size(max = 500, message = "설명은 500자 이하여야 합니다")
    val description: String? = null,

    @field:URL(message = "올바른 URL 형식이 아닙니다")
    val githubLink: String? = null,
)