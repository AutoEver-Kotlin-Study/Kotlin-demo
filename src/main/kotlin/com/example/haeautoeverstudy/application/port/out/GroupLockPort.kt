package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.GroupId

interface GroupLockPort {
    fun <T> withGroupLock(groupId: GroupId, action: () -> T): T
}
