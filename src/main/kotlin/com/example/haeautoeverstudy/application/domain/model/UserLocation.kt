package com.example.haeautoeverstudy.application.domain.model

import java.time.Instant

data class UserLocation(
    val userId: UserId,
    val location: GeoLocation,
    val updatedAt: Instant,
)
