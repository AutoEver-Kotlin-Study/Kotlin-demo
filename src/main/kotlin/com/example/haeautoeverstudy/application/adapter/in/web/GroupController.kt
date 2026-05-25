package com.example.haeautoeverstudy.application.adapter.`in`.web

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.GroupName
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.port.`in`.CreateGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.CreateGroupUseCase
import com.example.haeautoeverstudy.application.port.`in`.DeleteGroupCommand
import com.example.haeautoeverstudy.application.port.`in`.DeleteGroupUseCase
import com.example.haeautoeverstudy.application.port.`in`.GetUserGroupsCommand
import com.example.haeautoeverstudy.application.port.`in`.GetUserGroupsUseCase
import com.example.haeautoeverstudy.application.port.`in`.GroupDetail
import com.example.haeautoeverstudy.application.port.`in`.GroupSummary
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Tag(name = "Groups", description = "Group lifecycle APIs")
class GroupController(
    private val createGroupUseCase: CreateGroupUseCase,
    private val getUserGroupsUseCase: GetUserGroupsUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
) {
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create group", description = "Creates a group with an existing user as owner.")
    fun create(@RequestBody request: CreateGroupRequest): GroupResponse =
        createGroupUseCase.create(
            CreateGroupCommand(
                groupId = GroupId(request.groupId),
                ownerId = UserId(request.ownerId),
                name = GroupName(request.name),
                maxParticipantCount = request.maxParticipantCount,
            ),
        ).toResponse()

    @GetMapping("/users/{userId}/groups")
    @Operation(summary = "Get user groups", description = "Returns active groups the user belongs to.")
    fun getUserGroups(@PathVariable userId: String): List<GroupResponse> =
        getUserGroupsUseCase.getGroups(GetUserGroupsCommand(UserId(userId)))
            .map { it.toResponse() }

    @DeleteMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete group", description = "Deletes a group. Only the group owner can delete it.")
    fun delete(
        @PathVariable groupId: String,
        @RequestBody request: DeleteGroupRequest,
    ) {
        deleteGroupUseCase.delete(
            DeleteGroupCommand(
                groupId = GroupId(groupId),
                requestedBy = UserId(request.requestedBy),
            ),
        )
    }
}

data class CreateGroupRequest(
    val groupId: String,
    val ownerId: String,
    val name: String,
    val maxParticipantCount: Int,
)

data class DeleteGroupRequest(
    val requestedBy: String,
)

data class GroupResponse(
    val groupId: String,
    val ownerId: String,
    val name: String,
    val maxParticipantCount: Int,
    val currentParticipantCount: Int,
)

private fun GroupDetail.toResponse(): GroupResponse =
    GroupResponse(
        groupId = groupId.value,
        ownerId = ownerId.value,
        name = name.value,
        maxParticipantCount = maxParticipantCount,
        currentParticipantCount = currentParticipantCount,
    )

private fun GroupSummary.toResponse(): GroupResponse =
    GroupResponse(
        groupId = groupId.value,
        ownerId = ownerId.value,
        name = name.value,
        maxParticipantCount = maxParticipantCount,
        currentParticipantCount = currentParticipantCount,
    )
