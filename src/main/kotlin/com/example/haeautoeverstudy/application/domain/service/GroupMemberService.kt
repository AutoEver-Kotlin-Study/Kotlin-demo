package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.port.`in`.JoinGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.JoinGroupUseCase
import com.example.haeautoeverstudy.application.port.`in`.LeaveGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.LeaveGroupUseCase
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
class GroupMemberService(
    private val loadUserPort: LoadUserPort,
    private val loadMapGroupPort: LoadMapGroupPort,
    private val loadMapGroupsPort: LoadMapGroupsPort,
    private val saveMapGroupPort: SaveMapGroupPort,
    private val publishMapGroupEventPort: PublishMapGroupEventPort,
    private val groupLockPort: GroupLockPort,
    private val transactionTemplate: TransactionTemplate,
    private val userLocationPort: UserLocationPort,
) : JoinGroupUseCase, LeaveGroupUseCase {

    override fun join(command: JoinGroupCommand) {
        groupLockPort.withGroupLock(command.groupId) {

            //@Transactional 이용하면, lock을 대기하는 과정에서 커넥션풀을 잡고 있는 등의 연산 필요함.
            transactionTemplate.execute {
                loadUserPort.loadById(command.userId)
                val group = loadMapGroupPort.loadById(command.groupId)

                val event = group.addParticipant(command.userId)

                saveMapGroupPort.save(group)
                publishMapGroupEventPort.publish(event)
            }
        }
    }

    override fun leave(command: LeaveGroupCommand) {
        groupLockPort.withGroupLock(command.groupId) {
            transactionTemplate.execute {
                loadUserPort.loadById(command.userId)
                val group = loadMapGroupPort.loadById(command.groupId)

                val event = group.removeParticipant(command.userId)

                saveMapGroupPort.save(group)
                if (loadMapGroupsPort.loadActiveByParticipantId(command.userId).isEmpty()) {
                    userLocationPort.deleteByUserId(command.userId)
                }
                publishMapGroupEventPort.publish(event)
            }
        }
    }
}
