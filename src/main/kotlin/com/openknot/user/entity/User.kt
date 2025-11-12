package com.openknot.user.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table
class User (
    @Id
    private val id: UUID,
    var email: String,
    var password: String,
    var name: String,
    var profileImageUrl: String? = null,
    var description: String? = null,
    var githubLink: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var modifiedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
) : Persistable<UUID> {
    @field:Transient
    private var isNew: Boolean = true
    override fun getId(): UUID = this.id
    override fun isNew(): Boolean = this.isNew
    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }
}