package com.itau.authorizer.common.util.extension

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val zonedDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

fun ZonedDateTime.toIsoFormat(): String = this.format(zonedDateTimeFormatter)

fun LocalDate.atEndOfDay(zone: ZoneId): ZonedDateTime = this.plusDays(1).atStartOfDay(zone).minusNanos(1)
