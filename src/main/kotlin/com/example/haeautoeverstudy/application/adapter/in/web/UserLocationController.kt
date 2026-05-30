package com.example.haeautoeverstudy.application.adapter.`in`.web

import com.example.haeautoeverstudy.application.domain.model.GeoLocation
import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserLocation
import com.example.haeautoeverstudy.application.port.`in`.GetGroupUserLocationsCommand
import com.example.haeautoeverstudy.application.port.`in`.GetGroupUserLocationsUseCase
import com.example.haeautoeverstudy.application.port.`in`.UpdateUserLocationCommand
import com.example.haeautoeverstudy.application.port.`in`.UpdateUserLocationUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@Tag(name = "User Locations", description = "Realtime user location APIs")
class UserLocationController(
    private val updateUserLocationUseCase: UpdateUserLocationUseCase,
    private val getGroupUserLocationsUseCase: GetGroupUserLocationsUseCase,
) {
    @PutMapping("/api/users/{userId}/location")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update user location", description = "Updates a user's latest in-memory location snapshot.")
    fun update(
        @PathVariable userId: String,
        @RequestBody request: UpdateUserLocationRequest,
    ) {
        updateUserLocationUseCase.update(
            UpdateUserLocationCommand(
                userId = UserId(userId),
                location = GeoLocation(
                    latitude = request.latitude,
                    longitude = request.longitude,
                ),
            ),
        )
    }

    @GetMapping("/api/groups/{groupId}/locations")
    @Operation(summary = "Get group user locations", description = "Returns location snapshots for current group participants.")
    fun getGroupLocations(
        @PathVariable groupId: String,
    ): GroupUserLocationsResponse {
        val locations = getGroupUserLocationsUseCase.get(
            GetGroupUserLocationsCommand(
                groupId = GroupId(groupId),
            ),
        )

        return GroupUserLocationsResponse(
            groupId = groupId,
            locations = locations.map(UserLocationResponse::from),
        )
    }
}

data class UpdateUserLocationRequest(
    val latitude: Double,
    val longitude: Double,
)

data class GroupUserLocationsResponse(
    val groupId: String,
    val locations: List<UserLocationResponse>,
)

data class UserLocationResponse(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: Instant,
) {
    companion object {
        fun from(location: UserLocation): UserLocationResponse =
            UserLocationResponse(
                userId = location.userId.value,
                latitude = location.location.latitude,
                longitude = location.location.longitude,
                updatedAt = location.updatedAt,
            )
    }
}
