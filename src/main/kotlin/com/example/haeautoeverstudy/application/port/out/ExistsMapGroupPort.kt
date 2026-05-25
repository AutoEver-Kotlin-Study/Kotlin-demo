package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.GroupId

fun interface ExistsMapGroupPort {
    fun existsById(groupId: GroupId): Boolean
}
