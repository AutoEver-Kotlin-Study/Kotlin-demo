package com.example.haeautoeverstudy.application.adapter.out.persistence

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.port.out.ExistsMapGroupPort
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupPort
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupsPort
import com.example.haeautoeverstudy.application.port.out.SaveMapGroupPort
import org.springframework.stereotype.Component

@Component
class JpaMapGroupPersistenceAdapter(
    private val mapGroupJpaRepository: MapGroupJpaRepository,
) : LoadMapGroupPort, LoadMapGroupsPort, ExistsMapGroupPort, SaveMapGroupPort {

    override fun loadById(groupId: GroupId): MapGroup =
        mapGroupJpaRepository.findLockedById(groupId.value)
            .orElseThrow { NoSuchElementException("MapGroup[${groupId.value}] not found") }
            .toDomain()

    override fun loadAllByIds(groupIds: Set<GroupId>): List<MapGroup> {
        if (groupIds.isEmpty()) {
            return emptyList()
        }

        return mapGroupJpaRepository.findAllWithParticipantsByIdIn(groupIds.map { it.value })
            .map { it.toDomain() }
    }

    override fun existsById(groupId: GroupId): Boolean =
        mapGroupJpaRepository.existsById(groupId.value)

    override fun save(group: MapGroup) {
        val entity = mapGroupJpaRepository.findById(group.id.value)
            .orElseGet { MapGroupJpaEntity.from(group) }
        entity.updateFrom(group)
        mapGroupJpaRepository.save(entity)
    }
}
