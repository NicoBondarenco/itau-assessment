package com.itau.authorizer.common.util.extension

import java.time.ZonedDateTime
import java.util.UUID

fun String.toSnakeCase(): String = this.replace("(?<=.)[A-Z]".toRegex(), "_$0").lowercase()

fun String.toUUID(): UUID = UUID.fromString(this)

fun String.toZonedDateTime(): ZonedDateTime = ZonedDateTime.parse(this, zonedDateTimeFormatter)
