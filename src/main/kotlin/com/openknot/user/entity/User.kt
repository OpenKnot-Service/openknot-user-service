package com.openknot.user.entity

import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table
class User(
    @Id
    private val id: UUID,
    var email: String,
    var password: String,
    var name: String,

    var position: Position? = null,
    var detailedPosition: String? = null,

    var careerLevel: CareerLevel? = null,

    var profileImageUrl: String? = null,
    var description: String? = null,
    var githubLink: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var modifiedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
) : Persistable<UUID> {
    fun update(
        name: String? = null,
        position: String? = null,
        detailedPosition: String? = null,
        careerLevel: String? = null,
        profileImageUrl: String? = null,
        description: String? = null,
        githubLink: String? = null,
    ) {
        name?.let { this.name = it }
        position?.let {
            this.position = Position.fromLabel(it.uppercase())
                ?: throw BusinessException(ErrorCode.VALIDATION_FAIL)
        }
        detailedPosition?.let { this.detailedPosition = it }
        careerLevel?.let {
            this.careerLevel = CareerLevel.fromLabel(it.uppercase())
                ?: throw BusinessException(ErrorCode.VALIDATION_FAIL)
        }
        profileImageUrl?.let { this.profileImageUrl = it }
        description?.let { this.description = it }
        githubLink?.let { this.githubLink = it }
        this.modifiedAt = LocalDateTime.now()
    }

    override fun getId(): UUID = this.id
    override fun isNew(): Boolean = createdAt == null
    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }
}