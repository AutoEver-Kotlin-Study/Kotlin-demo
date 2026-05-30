package com.example.haeautoeverstudy.application.adapter.`in`.event

import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent
import com.example.haeautoeverstudy.application.port.`in`.HandleMapGroupEventUseCase
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class SpringMapGroupEventListener(
    private val handleMapGroupEventUseCase: HandleMapGroupEventUseCase,
) {

    //TODO 이벤트 발생 시, 트랜잭션도 롤백이 필요하면 phase 및 코루틴 수정 필요. -> 쓰레드가 달라지면 적용 되지 않음.
    //트랜잭션 성공 후, 이벤트 발행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: MapGroupEvent) {
        handleMapGroupEventUseCase.handle(event)
    }
}
