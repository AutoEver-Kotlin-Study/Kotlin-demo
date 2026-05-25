package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.exception.UserAlreadyExistsException
import com.example.haeautoeverstudy.application.port.`in`.RegisterUserCommand
import com.example.haeautoeverstudy.application.port.`in`.RegisterUserUseCase
import com.example.haeautoeverstudy.application.port.`in`.UserDetail
import com.example.haeautoeverstudy.application.port.out.ExistsUserPort
import com.example.haeautoeverstudy.application.port.out.SaveUserPort
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class UserRegistrationService(
    private val existsUserPort: ExistsUserPort,
    private val saveUserPort: SaveUserPort,
    private val transactionTemplate: TransactionTemplate,
) : RegisterUserUseCase {

    override fun register(command: RegisterUserCommand): UserDetail =
        transactionTemplate.execute {
            if (existsUserPort.existsByPhoneNumber(command.phoneNumber)) {
                throw UserAlreadyExistsException(command.phoneNumber.value)
            }

            User.of(
                name = command.name,
                phoneNumber = command.phoneNumber,
            ).also(saveUserPort::save)
                .toDetail()
        } ?: error("Register user transaction returned null")

    private fun User.toDetail(): UserDetail =
        UserDetail(
            userId = id,
            name = name,
            phoneNumber = phoneNumber,
        )
}
