package com.example.haeautoeverstudy.application.adapter.out.persistence

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.GroupName
import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.domain.model.UserId
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(
    name = "map_groups",
    indexes = [
        Index(name = "idx_map_groups_owner_id", columnList = "owner_id"),
    ],
)
class MapGroupJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: String = "",

    @Column(name = "owner_id", nullable = false)
    var ownerId: String = "",

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "max_participant_count", nullable = false)
    var maxParticipantCount: Int = 0,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "group_members",
        joinColumns = [JoinColumn(name = "group_id")],
        indexes = [
            Index(name = "idx_group_members_group_id", columnList = "group_id"),
            Index(name = "idx_group_members_user_id", columnList = "user_id"),
        ],
    )
    @Column(name = "user_id", nullable = false)
    var participantIds: MutableSet<String> = linkedSetOf(),
) {
    fun updateFrom(group: MapGroup) {
        ownerId = group.ownerId.value
        name = group.name.value
        maxParticipantCount = group.maxParticipantCount
        deleted = group.isDeleted
        participantIds.clear()
        participantIds.addAll(group.participants.map { it.value })
    }

    fun toDomain(): MapGroup =
        MapGroup.restore(
            id = GroupId(id),
            ownerId = UserId(ownerId),
            name = GroupName(name),
            maxParticipantCount = maxParticipantCount,
            participantIds = participantIds.map(::UserId).toSet(),
            deleted = deleted,
        )

    companion object {
        fun from(group: MapGroup): MapGroupJpaEntity =
            MapGroupJpaEntity(
                id = group.id.value,
                ownerId = group.ownerId.value,
                name = group.name.value,
                maxParticipantCount = group.maxParticipantCount,
                deleted = group.isDeleted,
                participantIds = group.participants.map { it.value }.toMutableSet(),
            )
    }
}
