package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent

fun interface HandleMapGroupEventUseCase {
    fun handle(event: MapGroupEvent)
}
