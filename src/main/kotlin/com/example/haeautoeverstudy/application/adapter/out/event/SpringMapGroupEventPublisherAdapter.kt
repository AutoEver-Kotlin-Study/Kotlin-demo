package com.example.haeautoeverstudy.application.adapter.out.event

import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent
import com.example.haeautoeverstudy.application.port.out.PublishMapGroupEventPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringMapGroupEventPublisherAdapter(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : PublishMapGroupEventPort {

    override fun publish(event: MapGroupEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
