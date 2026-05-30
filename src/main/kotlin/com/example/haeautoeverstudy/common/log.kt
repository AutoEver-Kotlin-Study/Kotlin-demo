package com.example.haeautoeverstudy.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

val Any.log: Logger
    get() = LoggerFactory.getLogger(this::class.java)

fun logger(type: KClass<*>): Logger = LoggerFactory.getLogger(type.java)

inline fun <reified T : Any> logger(): Logger = logger(T::class)
