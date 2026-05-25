package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.PhoneNumber

fun interface ExistsUserPort {
    fun existsByPhoneNumber(phoneNumber: PhoneNumber): Boolean
}
