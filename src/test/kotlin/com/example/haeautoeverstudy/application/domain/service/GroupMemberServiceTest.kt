package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.GroupName
import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent
import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserName
import com.example.haeautoeverstudy.application.domain.model.exception.GroupCapacityExceededException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupLockTimeoutException
import com.example.haeautoeverstudy.application.port.`in`.JoinGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.LeaveGroupCommand
import com.example.haeautoeverstudy.application.port.out.GroupLockPort
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupPort
import com.example.haeautoeverstudy.application.port.out.LoadUserPort
import com.example.haeautoeverstudy.application.port.out.PublishMapGroupEventPort
import com.example.haeautoeverstudy.application.port.out.SaveMapGroupPort
import com.example.haeautoeverstudy.application.port.out.SaveUserPort
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GroupMemberServiceTest {
    @Test
    fun `join loads aggregates with lock, saves them and publishes joined event`() {
        val groupId = GroupId("group")
        val user = user("participant")
        val group = group(groupId = groupId, maxParticipantCount = 2)
        val fixture = fixture(users = mutableMapOf(user.id to user), groups = mutableMapOf(groupId to group))

        fixture.service.join(JoinGroupCommand(userId = user.id, groupId = groupId))

        assertEquals(listOf(groupId), assertIs<RecordingLockPort>(fixture.lockPort).lockedGroupIds)
        assertEquals(listOf(user), fixture.saveUserPort.savedUsers)
        assertEquals(listOf(group), fixture.saveMapGroupPort.savedGroups)
        assertEquals(emptyList(), fixture.userLocationPort.deletedUserIds)
        assertTrue(user.isMemberOf(groupId))
        assertTrue(user.id in group.participants)

        val event = assertIs<MapGroupEvent.ParticipantJoined>(fixture.publishPort.publishedEvents.single())
        assertEquals(groupId, event.groupId)
        assertEquals(user.id, event.joinedUserId)
    }

    @Test
    fun `leave loads aggregates with lock, saves them and publishes left event`() {
        val groupId = GroupId("group")
        val user = user("participant")
        val group = group(groupId = groupId, maxParticipantCount = 2)
        group.addParticipant(user.id)
        user.joinGroup(groupId)
        val fixture = fixture(users = mutableMapOf(user.id to user), groups = mutableMapOf(groupId to group))

        fixture.service.leave(LeaveGroupCommand(userId = user.id, groupId = groupId))

        assertEquals(listOf(groupId), assertIs<RecordingLockPort>(fixture.lockPort).lockedGroupIds)
        assertEquals(listOf(user), fixture.saveUserPort.savedUsers)
        assertEquals(listOf(group), fixture.saveMapGroupPort.savedGroups)
        assertEquals(listOf(user.id), fixture.userLocationPort.deletedUserIds)
        assertTrue(!user.isMemberOf(groupId))
        assertTrue(user.id !in group.participants)

        val event = assertIs<MapGroupEvent.ParticipantLeft>(fixture.publishPort.publishedEvents.single())
        assertEquals(groupId, event.groupId)
        assertEquals(user.id, event.leftUserId)
    }

    @Test
    fun `join serializes requests per group and rejects users over capacity`() {
        val groupId = GroupId("group")
        val firstUser = user("first")
        val secondUser = user("second")
        val group = group(groupId = groupId, maxParticipantCount = 2)
        val fixture = fixture(
            lockPort = RealLockPort(),
            users = mutableMapOf(firstUser.id to firstUser, secondUser.id to secondUser),
            groups = mutableMapOf(groupId to group),
        )
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        val tasks = listOf(firstUser, secondUser).map { joiningUser ->
            executor.submit(
                Callable {
                    ready.countDown()
                    start.await(1, TimeUnit.SECONDS)
                    runCatching {
                        fixture.service.join(JoinGroupCommand(userId = joiningUser.id, groupId = groupId))
                    }
                },
            )
        }

        ready.await(1, TimeUnit.SECONDS)
        start.countDown()
        val results = tasks.map { it.get(1, TimeUnit.SECONDS) }
        executor.shutdown()

        assertEquals(1, results.count { it.isSuccess })
        assertEquals(1, results.count { it.exceptionOrNull() is GroupCapacityExceededException })
        assertEquals(2, group.currentParticipantCount)
        assertEquals(1, fixture.publishPort.publishedEvents.size)
    }

    @Test
    fun `group lock throws timeout exception when same group lock is not acquired in time`() {
        val groupId = GroupId("group")
        val lockPort = com.example.haeautoeverstudy.application.adapter.out.lock.InMemoryGroupLockAdapter(
            timeoutMillis = 50,
        )
        val lockAcquired = CountDownLatch(1)
        val releaseLock = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()

        val holder = executor.submit {
            lockPort.withGroupLock(groupId) {
                lockAcquired.countDown()
                releaseLock.await(1, TimeUnit.SECONDS)
            }
        }

        lockAcquired.await(1, TimeUnit.SECONDS)

        assertFailsWith<GroupLockTimeoutException> {
            lockPort.withGroupLock(groupId) {
                error("Should not acquire lock")
            }
        }

        releaseLock.countDown()
        holder.get(1, TimeUnit.SECONDS)
        executor.shutdown()
    }

    private fun fixture(
        lockPort: GroupLockPort = RecordingLockPort(),
        users: MutableMap<UserId, User>,
        groups: MutableMap<GroupId, MapGroup>,
    ): Fixture {
        val loadUserPort = FakeLoadUserPort(users)
        val loadMapGroupPort = FakeLoadMapGroupPort(groups)
        val saveUserPort = RecordingSaveUserPort()
        val saveMapGroupPort = RecordingSaveMapGroupPort()
        val publishPort = RecordingPublishMapGroupEventPort()
        val userLocationPort = RecordingUserLocationPort()
        val service = GroupMemberService(
            loadUserPort = loadUserPort,
            loadMapGroupPort = loadMapGroupPort,
            saveUserPort = saveUserPort,
            saveMapGroupPort = saveMapGroupPort,
            publishMapGroupEventPort = publishPort,
            groupLockPort = lockPort,
            transactionTemplate = TransactionTemplate(NoOpTransactionManager()),
            userLocationPort = userLocationPort,
        )

        return Fixture(service, lockPort, saveUserPort, saveMapGroupPort, publishPort, userLocationPort)
    }

    private data class Fixture(
        val service: GroupMemberService,
        val lockPort: GroupLockPort,
        val saveUserPort: RecordingSaveUserPort,
        val saveMapGroupPort: RecordingSaveMapGroupPort,
        val publishPort: RecordingPublishMapGroupEventPort,
        val userLocationPort: RecordingUserLocationPort,
    )

    private class FakeLoadUserPort(private val users: Map<UserId, User>) : LoadUserPort {
        override fun loadById(userId: UserId): User = users.getValue(userId)
    }

    private class FakeLoadMapGroupPort(private val groups: Map<GroupId, MapGroup>) : LoadMapGroupPort {
        override fun loadById(groupId: GroupId): MapGroup = groups.getValue(groupId)
    }

    private class RecordingSaveUserPort : SaveUserPort {
        val savedUsers: MutableList<User> = Collections.synchronizedList(mutableListOf())

        override fun save(user: User) {
            savedUsers += user
        }
    }

    private class RecordingSaveMapGroupPort : SaveMapGroupPort {
        val savedGroups: MutableList<MapGroup> = Collections.synchronizedList(mutableListOf())

        override fun save(group: MapGroup) {
            savedGroups += group
        }
    }

    private class RecordingPublishMapGroupEventPort : PublishMapGroupEventPort {
        val publishedEvents: MutableList<MapGroupEvent> = Collections.synchronizedList(mutableListOf())

        override fun publish(event: MapGroupEvent) {
            publishedEvents += event
        }
    }

    private class RecordingUserLocationPort : UserLocationPort {
        val deletedUserIds = mutableListOf<UserId>()

        override fun save(location: com.example.haeautoeverstudy.application.domain.model.UserLocation) = Unit

        override fun loadByUserIds(userIds: Set<UserId>): List<com.example.haeautoeverstudy.application.domain.model.UserLocation> =
            emptyList()

        override fun deleteByUserId(userId: UserId) {
            deletedUserIds += userId
        }
    }

    private class RecordingLockPort : GroupLockPort {
        val lockedGroupIds = mutableListOf<GroupId>()

        override fun <T> withGroupLock(groupId: GroupId, action: () -> T): T {
            lockedGroupIds += groupId
            return action()
        }
    }

    private class RealLockPort : GroupLockPort {
        private val delegate = com.example.haeautoeverstudy.application.adapter.out.lock.InMemoryGroupLockAdapter(
            timeoutMillis = 1000,
        )

        override fun <T> withGroupLock(groupId: GroupId, action: () -> T): T =
            delegate.withGroupLock(groupId, action)
    }

    private class NoOpTransactionManager : PlatformTransactionManager {
        override fun getTransaction(definition: TransactionDefinition?): TransactionStatus =
            SimpleTransactionStatus()

        override fun commit(status: TransactionStatus) = Unit

        override fun rollback(status: TransactionStatus) = Unit
    }

    private fun user(id: String): User =
        User.of(
            id = UserId(id),
            name = UserName("${id}User"),
            phoneNumber = PhoneNumber("01012345678"),
        )

    private fun group(groupId: GroupId, maxParticipantCount: Int): MapGroup =
        MapGroup.of(
            id = groupId,
            ownerId = UserId("owner"),
            name = GroupName("family"),
            maxParticipantCount = maxParticipantCount,
        )
}
