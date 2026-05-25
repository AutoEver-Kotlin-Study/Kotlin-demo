package com.example.haeautoeverstudy.application.adapter.`in`.web

import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.UserName
import com.example.haeautoeverstudy.application.port.`in`.RegisterUserCommand
import com.example.haeautoeverstudy.application.port.`in`.RegisterUserUseCase
import com.example.haeautoeverstudy.application.port.`in`.UserDetail
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User APIs")
class UserController(
    private val registerUserUseCase: RegisterUserUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register user", description = "Creates a user with name and phone number.")
    fun register(@RequestBody request: RegisterUserRequest): UserResponse =
        registerUserUseCase.register(
            RegisterUserCommand(
                name = UserName(request.name),
                phoneNumber = PhoneNumber(request.phoneNumber),
            ),
        ).toResponse()
}

data class RegisterUserRequest(
    val name: String,
    val phoneNumber: String,
)

data class UserResponse(
    val userId: String,
    val name: String,
    val phoneNumber: String,
)

private fun UserDetail.toResponse(): UserResponse =
    UserResponse(
        userId = userId.value,
        name = name.value,
        phoneNumber = phoneNumber.value,
    )
