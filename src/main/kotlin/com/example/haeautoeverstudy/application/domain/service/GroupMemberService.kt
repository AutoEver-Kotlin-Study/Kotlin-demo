package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent
import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.exception.MembershipStateMismatchException
import com.example.haeautoeverstudy.application.port.`in`.JoinGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.JoinGroupUseCase
import com.example.haeautoeverstudy.application.port.`in`.LeaveGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.LeaveGroupUseCase
import com.example.haeautoeverstudy.application.port.out.GroupLockPort
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupPort
import com.example.haeautoeverstudy.application.port.out.LoadUserPort
import com.example.haeautoeverstudy.application.port.out.PublishMapGroupEventPort
import com.example.haeautoeverstudy.application.port.out.SaveMapGroupPort
import com.example.haeautoeverstudy.application.port.out.SaveUserPort
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class GroupMemberService(
    private val loadUserPort: LoadUserPort,
    private val loadMapGroupPort: LoadMapGroupPort,
    private val saveUserPort: SaveUserPort,
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
                val user = loadUserPort.loadById(command.userId)
                val group = loadMapGroupPort.loadById(command.groupId)

                val event = join(user, group)

                saveUserPort.save(user)
                saveMapGroupPort.save(group)
                publishMapGroupEventPort.publish(event)
            }
        }
    }

    override fun leave(command: LeaveGroupCommand) {
        groupLockPort.withGroupLock(command.groupId) {
            transactionTemplate.execute {
                val user = loadUserPort.loadById(command.userId)
                val group = loadMapGroupPort.loadById(command.groupId)

                val event = leave(user, group)

                saveUserPort.save(user)
                saveMapGroupPort.save(group)
                userLocationPort.deleteByGroupIdAndUserId(group.id, user.id)
                publishMapGroupEventPort.publish(event)
            }
        }
    }

    private fun join(user: User, group: MapGroup): MapGroupEvent.ParticipantJoined {
        if (user.isMemberOf(group.id)) {
            throw MembershipStateMismatchException(user.id.value, group.id.value)
        }

        val event = group.addParticipant(user.id)
        user.joinGroup(group.id)
        return event
    }

    private fun leave(user: User, group: MapGroup): MapGroupEvent.ParticipantLeft {
        group.assertParticipant(user.id)

        if (!user.isMemberOf(group.id)) {
            throw MembershipStateMismatchException(user.id.value, group.id.value)
        }

        val event = group.removeParticipant(user.id)
        user.leaveGroup(group.id)
        return event
    }
}
