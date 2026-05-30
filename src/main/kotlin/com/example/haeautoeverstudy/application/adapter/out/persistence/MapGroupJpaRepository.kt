package com.example.haeautoeverstudy.application.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface MapGroupJpaRepository : JpaRepository<MapGroupJpaEntity, String> {

    //선착순을 보장해야하므로 비관적 락이용. scale out 이 일어나지 않는다면 낙관적 락도 괜찮음.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct g from MapGroupJpaEntity g left join fetch g.participantIds where g.id = :id")
    fun findLockedById(@Param("id") id: String): Optional<MapGroupJpaEntity>

    @Query("select distinct g from MapGroupJpaEntity g left join fetch g.participantIds where g.id in :ids")
    fun findAllWithParticipantsByIdIn(@Param("ids") ids: Collection<String>): List<MapGroupJpaEntity>

    @Query(
        """
        select distinct g
        from MapGroupJpaEntity g
        left join fetch g.participantIds
        where :userId member of g.participantIds
          and g.deleted = false
        """,
    )
    fun findAllActiveByParticipantId(@Param("userId") userId: String): List<MapGroupJpaEntity>
}
