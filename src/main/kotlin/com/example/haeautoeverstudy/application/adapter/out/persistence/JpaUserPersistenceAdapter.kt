package com.example.haeautoeverstudy.application.adapter.out.persistence

import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.port.out.ExistsUserPort
import com.example.haeautoeverstudy.application.port.out.LoadUserPort
import com.example.haeautoeverstudy.application.port.out.LoadUsersPort
import com.example.haeautoeverstudy.application.port.out.SaveUserPort
import org.springframework.stereotype.Component

@Component
class JpaUserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : LoadUserPort, LoadUsersPort, ExistsUserPort, SaveUserPort {

    override fun loadById(userId: UserId): User =
        userJpaRepository.findLockedById(userId.value)
            .orElseThrow { NoSuchElementException("User[${userId.value}] not found") }
            .toDomain()

    override fun loadAllByIds(userIds: Set<UserId>): List<User> {
        if (userIds.isEmpty()) {
            return emptyList()
        }

        return userJpaRepository.findAllLockedByIdIn(userIds.map { it.value })
            .map { it.toDomain() }
    }

    override fun existsByPhoneNumber(phoneNumber: PhoneNumber): Boolean =
        userJpaRepository.existsByPhoneNumber(phoneNumber.value)

    override fun save(user: User) {
        val entity = userJpaRepository.findById(user.id.value)
            .orElseGet { UserJpaEntity.from(user) }
        entity.updateFrom(user)
        userJpaRepository.save(entity)
    }
}
