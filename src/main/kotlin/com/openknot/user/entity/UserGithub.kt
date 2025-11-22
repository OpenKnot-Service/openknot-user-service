package com.openknot.user.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table
class UserGithub(
    @Id
    private val id: UUID,
    var userId: UUID,
    var githubId: Long,
    var githubUsername: String,
    var githubAccessToken: String,
    var avatarUrl: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var modifiedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
) : Persistable<UUID> {
    override fun getId(): UUID = this.id
    override fun isNew(): Boolean = createdAt == null
    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserGithub
        return id == other.id
    }
}