package com.example.haeautoeverstudy.application.adapter.`in`.web

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.port.`in`.JoinGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.JoinGroupUseCase
import com.example.haeautoeverstudy.application.port.`in`.LeaveGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.LeaveGroupUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/groups/{groupId}/members")
@Tag(name = "Group Members", description = "Group membership APIs")
class GroupMemberController(
    private val joinGroupUseCase: JoinGroupUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Join group", description = "Adds a user to the group if capacity allows.")
    fun join(
        @PathVariable groupId: String,
        @RequestBody request: GroupMemberRequest,
    ) {
        joinGroupUseCase.join(
            JoinGroupCommand(
                groupId = GroupId(groupId),
                userId = UserId(request.userId),
            ),
        )
    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Leave group", description = "Removes a user from the group and clears the user's group location.")
    fun leave(
        @PathVariable groupId: String,
        @RequestBody request: GroupMemberRequest,
    ) {
        leaveGroupUseCase.leave(
            LeaveGroupCommand(
                groupId = GroupId(groupId),
                userId = UserId(request.userId),
            ),
        )
    }
}

data class GroupMemberRequest(
    val userId: String,
)
