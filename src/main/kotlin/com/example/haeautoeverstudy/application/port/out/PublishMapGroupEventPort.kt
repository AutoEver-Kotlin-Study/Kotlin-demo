package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent

fun interface PublishMapGroupEventPort {
    fun publish(event: MapGroupEvent)
}
