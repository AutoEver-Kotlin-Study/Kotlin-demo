package com.example.haeautoeverstudy.application.adapter.out.lock

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.exception.GroupLockTimeoutException
import com.example.haeautoeverstudy.application.port.out.GroupLockPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Component
class InMemoryGroupLockAdapter(
    @Value("\${app.group-lock.timeout-millis:3000}")
    private val timeoutMillis: Long,
) : GroupLockPort {

    //TODO OOM 방지 필요
    private val locks = ConcurrentHashMap<GroupId, ReentrantLock>()

    //TODO 추후 scale out 고려하면, redis 등으로 변경할 수 있도록 설계 필요
    override fun <T> withGroupLock(groupId: GroupId, action: () -> T): T {
        val lock = locks.computeIfAbsent(groupId) {
            ReentrantLock(true)
        }

        return lock.withTimeout(groupId, timeoutMillis, action)
    }

    private fun <T> ReentrantLock.withTimeout(
        groupId: GroupId,
        timeoutMillis: Long,
        action: () -> T,
    ): T {
        if (!tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
            throw GroupLockTimeoutException(groupId.value, timeoutMillis)
        }

        return try {
            action()
        } finally {
            unlock()
        }
    }
}
