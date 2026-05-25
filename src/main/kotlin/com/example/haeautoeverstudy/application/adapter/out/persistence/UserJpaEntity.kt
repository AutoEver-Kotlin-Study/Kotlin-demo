package com.example.haeautoeverstudy.application.adapter.out.persistence

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserName
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: String = "",

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "phone_number", nullable = false, unique = true)
    var phoneNumber: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_group_memberships",
        joinColumns = [JoinColumn(name = "user_id")],
        indexes = [
            Index(name = "idx_user_group_memberships_user_id", columnList = "user_id"),
            Index(name = "idx_user_group_memberships_group_id", columnList = "group_id"),
        ],
    )
    @Column(name = "group_id", nullable = false)
    var joinedGroupIds: MutableSet<String> = linkedSetOf(),
) {
    fun updateFrom(user: User) {
        name = user.name.value
        phoneNumber = user.phoneNumber.value
        joinedGroupIds.clear()
        joinedGroupIds.addAll(user.groupIds.map { it.value })
    }

    fun toDomain(): User =
        User.restore(
            id = UserId(id),
            name = UserName(name),
            phoneNumber = PhoneNumber(phoneNumber),
            joinedGroupIds = joinedGroupIds.map(::GroupId).toSet(),
        )

    companion object {
        fun from(user: User): UserJpaEntity =
            UserJpaEntity(
                id = user.id.value,
                name = user.name.value,
                phoneNumber = user.phoneNumber.value,
                joinedGroupIds = user.groupIds.map { it.value }.toMutableSet(),
            )
    }
}
