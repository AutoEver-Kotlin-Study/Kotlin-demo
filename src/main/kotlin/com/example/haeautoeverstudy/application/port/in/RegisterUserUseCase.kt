package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserName

fun interface RegisterUserUseCase {
    fun register(command: RegisterUserCommand): UserDetail
}

data class RegisterUserCommand(
    val name: UserName,
    val phoneNumber: PhoneNumber,
)

data class UserDetail(
    val userId: UserId,
    val name: UserName,
    val phoneNumber: PhoneNumber,
)
