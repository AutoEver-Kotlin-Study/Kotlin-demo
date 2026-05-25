package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface LoadUsersPort {
    fun loadAllByIds(userIds: Set<UserId>): List<User>
}
