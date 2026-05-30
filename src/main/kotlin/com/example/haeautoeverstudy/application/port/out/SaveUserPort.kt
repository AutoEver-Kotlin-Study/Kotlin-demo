package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.User

fun interface SaveUserPort {
    fun save(user: User)
}
