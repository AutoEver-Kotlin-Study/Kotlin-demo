package com.example.haeautoeverstudy.application.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, String> {
    fun findByIdIn(ids: Collection<String>): List<UserJpaEntity>

    fun existsByPhoneNumber(phoneNumber: String): Boolean
}
