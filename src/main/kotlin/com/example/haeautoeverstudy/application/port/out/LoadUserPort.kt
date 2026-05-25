package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface LoadUserPort {
    fun loadById(userId: UserId): User
}
