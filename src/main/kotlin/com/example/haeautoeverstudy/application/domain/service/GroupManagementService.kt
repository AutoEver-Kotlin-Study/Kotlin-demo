package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.domain.model.exception.GroupAlreadyExistsException
import com.example.haeautoeverstudy.application.port.`in`.CreateGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.CreateGroupUseCase
import com.example.haeautoeverstudy.application.port.`in`.DeleteGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.DeleteGroupUseCase
import com.example.haeautoeverstudy.application.port.`in`.GetUserGroupsCommand
import com.example.haeautoeverstudy.application.port.`in`.GetUserGroupsUseCase
import com.example.haeautoeverstudy.application.port.`in`.GroupDetail
import com.example.haeautoeverstudy.application.port.`in`.GroupSummary
import com.example.haeautoeverstudy.application.port.out.ExistsMapGroupPort
import com.example.haeautoeverstudy.application.port.out.GroupLockPort
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupPort
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupsPort
import com.example.haeautoeverstudy.application.port.out.LoadUserPort
import com.example.haeautoeverstudy.application.port.out.PublishMapGroupEventPort
import com.example.haeautoeverstudy.application.port.out.SaveMapGroupPort
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class GroupManagementService(
    private val loadUserPort: LoadUserPort,
    private val loadMapGroupPort: LoadMapGroupPort,
    private val loadMapGroupsPort: LoadMapGroupsPort,
    private val existsMapGroupPort: ExistsMapGroupPort,
    private val saveMapGroupPort: SaveMapGroupPort,
    private val publishMapGroupEventPort: PublishMapGroupEventPort,
    private val groupLockPort: GroupLockPort,
    private val userLocationPort: UserLocationPort,
    private val transactionTemplate: TransactionTemplate,
) : CreateGroupUseCase, GetUserGroupsUseCase, DeleteGroupUseCase {

    override fun create(command: CreateGroupCommand): GroupDetail =
        groupLockPort.withGroupLock(command.groupId) {
            transactionTemplate.execute {
                if (existsMapGroupPort.existsById(command.groupId)) {
                    throw GroupAlreadyExistsException(command.groupId.value)
                }

                loadUserPort.loadById(command.ownerId)
                val group = MapGroup.of(
                    id = command.groupId,
                    ownerId = command.ownerId,
                    name = command.name,
                    maxParticipantCount = command.maxParticipantCount,
                )

                saveMapGroupPort.save(group)

                group.toDetail()
            } ?: error("Create group transaction returned null")
        }

    override fun getGroups(command: GetUserGroupsCommand): List<GroupSummary> =
        transactionTemplate.execute {
            loadUserPort.loadById(command.userId)
            loadMapGroupsPort.loadActiveByParticipantId(command.userId)
                .map { it.toSummary() }
        } ?: emptyList()

    override fun delete(command: DeleteGroupCommand) {
        groupLockPort.withGroupLock(command.groupId) {
            transactionTemplate.execute {
                val group = loadMapGroupPort.loadById(command.groupId)
                val participantIds = group.participants
                val event = group.delete(command.requestedBy)

                saveMapGroupPort.save(group)

                participantIds.forEach { participantId ->
                    if (loadMapGroupsPort.loadActiveByParticipantId(participantId).isEmpty()) {
                        userLocationPort.deleteByUserId(participantId)
                    }
                }

                publishMapGroupEventPort.publish(event)
            }
        }
    }

    private fun MapGroup.toDetail(): GroupDetail =
        GroupDetail(
            groupId = id,
            ownerId = ownerId,
            name = name,
            maxParticipantCount = maxParticipantCount,
            currentParticipantCount = currentParticipantCount,
        )

    private fun MapGroup.toSummary(): GroupSummary =
        GroupSummary(
            groupId = id,
            ownerId = ownerId,
            name = name,
            maxParticipantCount = maxParticipantCount,
            currentParticipantCount = currentParticipantCount,
        )
}
