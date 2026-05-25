package com.example.haeautoeverstudy.application.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UserJpaRepository : JpaRepository<UserJpaEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct u from UserJpaEntity u left join fetch u.joinedGroupIds where u.id = :id")
    fun findLockedById(@Param("id") id: String): Optional<UserJpaEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct u from UserJpaEntity u left join fetch u.joinedGroupIds where u.id in :ids")
    fun findAllLockedByIdIn(@Param("ids") ids: Collection<String>): List<UserJpaEntity>

    fun existsByPhoneNumber(phoneNumber: String): Boolean
}
