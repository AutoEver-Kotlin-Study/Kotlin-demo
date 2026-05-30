package com.example.haeautoeverstudy.application.adapter.out.persistence

import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
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
) {
    fun updateFrom(user: User) {
        name = user.name.value
        phoneNumber = user.phoneNumber.value
    }

    fun toDomain(): User =
        User.restore(
            id = UserId(id),
            name = UserName(name),
            phoneNumber = PhoneNumber(phoneNumber),
        )

    companion object {
        fun from(user: User): UserJpaEntity =
            UserJpaEntity(
                id = user.id.value,
                name = user.name.value,
                phoneNumber = user.phoneNumber.value,
            )
    }
}
