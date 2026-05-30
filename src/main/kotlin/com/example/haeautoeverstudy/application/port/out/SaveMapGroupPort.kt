package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.MapGroup

fun interface SaveMapGroupPort {
    fun save(group: MapGroup)
}
